package org.leolo.nrinfo.util;


import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

public class StompUtil {

    public static String getMessageBody(Message message) throws JMSException {
        if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            long length = bytesMessage.getBodyLength();
            byte [] data = new byte[(int) length];
            bytesMessage.readBytes(data);
            return new String(data);
        } else {
            throw new JMSException("Message is not a BytesMessage");
        }
    }

}
