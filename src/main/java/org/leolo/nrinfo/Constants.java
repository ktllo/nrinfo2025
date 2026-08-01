package org.leolo.nrinfo;

public class Constants {
    public static class EpsgCode {
        public static final int WGS84 = 4326;
        public static final int UKOS = 27700;
    }

    public static class Identifier {
        public static final String AUTH_COOKIE_NAME = "nrinfo2025_auth";
        public static class Session {
            public static final String USER_ID = "auth_user_id";
        }
    }

    public static class Model {
        public static final String GENERIC_POPUP_MESSAGE = "generic_popup_message";
    }

    public static class AIPrompt {
        public static final String SYSTEM_PERFORMANCE_SUMMARY = """
                Generate a brief summary of the performance of the following train operators
                
                ## Rules
                * DO NOT explain background info
                * Do not quote exact numbers, you may quite percentage
                * Do not ask further questions
                * Always give short comments
                
                ## Background info
                * Data are based on the arrival time at final destination
                * Threshold expressed in minutes
                * Threshold is 0 means missing data
                * Delay in exceed of 2 hours are considered as cancelled
                * Data only includes trains departs after 02:00 today
                * 90% on time is bad
                """;
    }
}
