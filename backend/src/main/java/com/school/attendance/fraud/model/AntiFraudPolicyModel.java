package com.school.attendance.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AntiFraudPolicyModel {

    private MethodConfig deviceCheck = new MethodConfig(true, false);
    private QrConfig qrCheck = new QrConfig(true, true, 15);
    private SixDigitConfig sixDigitCheck = new SixDigitConfig(true, false, 30);
    private GpsConfig gpsCheck = new GpsConfig(true, false, 150.0, 80.0, 32.0575, 35.8890);
    private ManualReviewConfig manualReview = new ManualReviewConfig(true, 14, true);

    public AntiFraudPolicyModel() {}

    public MethodConfig getDeviceCheck() {
        return deviceCheck;
    }

    public void setDeviceCheck(MethodConfig deviceCheck) {
        this.deviceCheck = deviceCheck;
    }

    public QrConfig getQrCheck() {
        return qrCheck;
    }

    public void setQrCheck(QrConfig qrCheck) {
        this.qrCheck = qrCheck;
    }

    public SixDigitConfig getSixDigitCheck() {
        return sixDigitCheck;
    }

    public void setSixDigitCheck(SixDigitConfig sixDigitCheck) {
        this.sixDigitCheck = sixDigitCheck;
    }

    public GpsConfig getGpsCheck() {
        return gpsCheck;
    }

    public void setGpsCheck(GpsConfig gpsCheck) {
        this.gpsCheck = gpsCheck;
    }

    public ManualReviewConfig getManualReview() {
        return manualReview;
    }

    public void setManualReview(ManualReviewConfig manualReview) {
        this.manualReview = manualReview;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MethodConfig {
        private boolean enabled = true;
        private boolean required = false;

        public MethodConfig() {}

        public MethodConfig(boolean enabled, boolean required) {
            this.enabled = enabled;
            this.required = required;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QrConfig extends MethodConfig {
        private int rotationSeconds = 15;

        public QrConfig() {
            super(true, true);
        }

        public QrConfig(boolean enabled, boolean required, int rotationSeconds) {
            super(enabled, required);
            this.rotationSeconds = rotationSeconds;
        }

        public int getRotationSeconds() {
            return rotationSeconds;
        }

        public void setRotationSeconds(int rotationSeconds) {
            this.rotationSeconds = rotationSeconds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SixDigitConfig extends MethodConfig {
        private int expirationSeconds = 30;

        public SixDigitConfig() {
            super(true, false);
        }

        public SixDigitConfig(boolean enabled, boolean required, int expirationSeconds) {
            super(enabled, required);
            this.expirationSeconds = expirationSeconds;
        }

        public int getExpirationSeconds() {
            return expirationSeconds;
        }

        public void setExpirationSeconds(int expirationSeconds) {
            this.expirationSeconds = expirationSeconds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GpsConfig extends MethodConfig {
        private double radiusMeters = 150.0;
        private double maxAccuracyMeters = 80.0;
        private double schoolLatitude = 32.0575;
        private double schoolLongitude = 35.8890;

        public GpsConfig() {
            super(true, false);
        }

        public GpsConfig(boolean enabled, boolean required, double radiusMeters, double maxAccuracyMeters, double schoolLatitude, double schoolLongitude) {
            super(enabled, required);
            this.radiusMeters = radiusMeters;
            this.maxAccuracyMeters = maxAccuracyMeters;
            this.schoolLatitude = schoolLatitude;
            this.schoolLongitude = schoolLongitude;
        }

        public double getRadiusMeters() {
            return radiusMeters;
        }

        public void setRadiusMeters(double radiusMeters) {
            this.radiusMeters = radiusMeters;
        }

        public double getMaxAccuracyMeters() {
            return maxAccuracyMeters;
        }

        public void setMaxAccuracyMeters(double maxAccuracyMeters) {
            this.maxAccuracyMeters = maxAccuracyMeters;
        }

        public double getSchoolLatitude() {
            return schoolLatitude;
        }

        public void setSchoolLatitude(double schoolLatitude) {
            this.schoolLatitude = schoolLatitude;
        }

        public double getSchoolLongitude() {
            return schoolLongitude;
        }

        public void setSchoolLongitude(double schoolLongitude) {
            this.schoolLongitude = schoolLongitude;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManualReviewConfig {
        private boolean enabled = true;
        private int initialPeriodDays = 14;
        private boolean requireReviewForFlagged = true;

        public ManualReviewConfig() {}

        public ManualReviewConfig(boolean enabled, int initialPeriodDays, boolean requireReviewForFlagged) {
            this.enabled = enabled;
            this.initialPeriodDays = initialPeriodDays;
            this.requireReviewForFlagged = requireReviewForFlagged;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getInitialPeriodDays() {
            return initialPeriodDays;
        }

        public void setInitialPeriodDays(int initialPeriodDays) {
            this.initialPeriodDays = initialPeriodDays;
        }

        public boolean isRequireReviewForFlagged() {
            return requireReviewForFlagged;
        }

        public void setRequireReviewForFlagged(boolean requireReviewForFlagged) {
            this.requireReviewForFlagged = requireReviewForFlagged;
        }
    }
}
