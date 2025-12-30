package org.leolo.nrinfo.irc;

import org.leolo.nrinfo.irc.annotation.Command;
import org.leolo.nrinfo.irc.annotation.IrcController;
import org.leolo.nrinfo.service.ConfigurationService;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class IrcConnector {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Hashtable<String, List<CommandOption>> commandMap = new Hashtable<>();
    private ConcurrentSkipListMap<String, String> aliasMap = new ConcurrentSkipListMap<>();
    @Autowired private ConfigurationService configService;
    private boolean initialized = false;

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        logger.info("Checking if IRC connection is required");
        List<String> networks = configService.getList("irc.networks");
        logger.info("{} networks found", networks.size());
        if (configService.getBoolean("irc.enabled") && !networks.isEmpty()) {
            init();
        }
    }

    private synchronized  void init() {
        if (initialized) {
            return;
        }
        logger.info("Scanning for commands for IRC");
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IrcController.class));
        HashMap<String, List<Method>> commandMap = new HashMap<>();
        for (BeanDefinition bd : scanner.findCandidateComponents("org.leolo.nrinfo")){
            logger.info("IrcController {} found",bd.getBeanClassName());
            try {
                Class<? extends Object> clazz = Class.forName(bd.getBeanClassName());
                IrcController ircController = clazz.getAnnotation(IrcController.class);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Command.class)) {
                        Command cmd = method.getAnnotation(Command.class);
                        String cmdName = normalizeCommandName(cmd.value());
                        logger.debug("Irc Command {} found", cmdName);
                        List<Method> methods;
                        if (commandMap.containsKey(cmdName)) {
                            methods = commandMap.get(cmdName);
                        } else {
                            methods = new Vector<>();
                            commandMap.put(cmdName, methods);
                        }
                        methods.add(method);
                    }
                }
            } catch (ClassNotFoundException e) {
                // This should not be happens, go onto next one
                logger.error("Unable to find IRC controller {}", bd.getBeanClassName());
            }
        }
        //Scan for duplication/incorrect method signature
        //Must return void
        //Acceptable signature
        // (specific event, String)
        // (specific event)
        // (Event, String)
        // (Event)
        // (void)
        for(String command: commandMap.keySet()) {
            List<Method> methods = commandMap.get(command);
            logger.debug("Checking methods for command {}, {} methods registered", command, methods.size());
            List<CommandOption> filteredMethods = new Vector<>();
            for(Method method:methods) {
                if (!method.getReturnType().equals(Void.TYPE)) {
                    logger.warn("Method {}.{}() returns {} which is not acceptable", method.getDeclaringClass().getName(), method.getName(), method.getReturnType());
                    continue;
                }
                if (method.getParameterCount() == 0) {
                    //It's always OK
                    filteredMethods.add(new CommandOption(method, Integer.MAX_VALUE));
                } else if (method.getParameterCount() == 1) {
                    //Check parameter 1
                    if (method.getParameterTypes()[0].equals(Event.class)) {
                        filteredMethods.add(new CommandOption(method, 1000));
                    } else if (checkEventType(method.getParameters()[0])) {
                        filteredMethods.add(new CommandOption(method, 800));
                    }
                } else if (method.getParameterCount() == 2) {
                    //Check parameter 1 and 2
                    if (method.getParameterTypes()[1].equals(String.class)) {
                        if (method.getParameterTypes()[0].equals(Event.class)) {
                            filteredMethods.add(new CommandOption(method, 900));
                        } else if (checkEventType(method.getParameters()[0])) {
                            filteredMethods.add(new CommandOption(method, 700));
                        }
                    }
                }
            }
            logger.info("Command {} has {} handler", command, filteredMethods.size());
            this.commandMap.put(command, filteredMethods);
        }
        initialized = true;
    }

    private boolean checkEventType(Parameter parameter) {
        return parameter.getType().isAssignableFrom(Event.class);
    }

    private String normalizeCommandName(String commandName) {
        return commandName.toLowerCase().strip();
    }

    static class CommandOption implements Comparable<CommandOption> {
        Method method;
        int priority;

        @Override
        public int compareTo(CommandOption o) {
            return Integer.compare(priority, o.priority);
        }

        public CommandOption(Method method, int priority) {
            this.method = method;
            this.priority = priority;
        }
    }

}
