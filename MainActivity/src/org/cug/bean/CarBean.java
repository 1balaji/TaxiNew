package org.cug.bean;

/**
 * Created with IntelliJ IDEA.
 * User: liulin
 * Date: 13-4-25
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class CarBean {
    private String carName;
    private String latitude;
    private String longitude;
    private String distance;
    private String phoneNum;

    public CarBean() {

    }


    public CarBean(String carName, String latitude, String longitude, String distance, String phoneNum) {
        super();
        this.carName = carName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.phoneNum = phoneNum;

    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }


}
