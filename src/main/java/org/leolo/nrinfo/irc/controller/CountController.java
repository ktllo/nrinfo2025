package org.leolo.nrinfo.irc.controller;

import org.leolo.nrinfo.irc.annotation.Command;
import org.leolo.nrinfo.irc.annotation.IrcController;
import org.leolo.nrinfo.service.CounterService;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;

@IrcController
public class CountController {

    @Autowired
    private CounterService counterService;

    @Command("counter")
    public String counter(GenericMessageEvent event) {
        return Integer.toString(counterService.getCounter("TEST"));
    }

}
