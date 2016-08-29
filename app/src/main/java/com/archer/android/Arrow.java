package com.archer.android;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by davidemelianov on 1/22/16.
 */
public class Arrow implements Parcelable {

    public String mType;
    //REQUEST for new requests
    //ACTIVE for currently running arrows
    //SPONSORED for purple places
    //PROMPT for the "send a new arrow" item
    //SEPARATOR for the banners
    //PERSON for the send arrows screen with contacts

    public String mName;
    public String mMID;

    public String mAID;

    public String mNumber;

    public Location mDestinationLocation = new Location("server");
    Double mLatitude = 0.0;
    Double mLongitude = 0.0;

    public Date mExpirationTime;

    public Arrow(String type) {
        this.mType = type;
    }

    public void setName(String name) { this.mName = name; }
    public String getName() { return this.mName; }

    public void setMID(String mid) { this.mMID = mid; }
    public String getMID() { return this.mMID; }

    public void setAID(String aid) { this.mAID = aid; }
    public String getAID() {
        if (this.mAID == null) {
            return "";
        } else {
            return this.mAID;
        }
    }

    public void setNumber(String number) { this.mNumber = number; }
    public String getNumber() { return this.mNumber; }

    public void setDestinationLocation(Double latitude, Double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mDestinationLocation.setLatitude(latitude);
        this.mDestinationLocation.setLongitude(longitude);
    }

    public float getFloatDistanceTo(Location mStartLocation) {

        return this.mDestinationLocation.distanceTo(mStartLocation);

    }

    public String getDistanceTo(Location mStartLocation) {

        float mFloatDistance = getFloatDistanceTo(mStartLocation);
        Integer mDistanceValueInt = Math.round(mFloatDistance);
        if (mDistanceValueInt > 1000) {
            return "1000+";
        } else  {
            return mDistanceValueInt.toString();
        }

    }

    public Double getLatitude() { return this.mLatitude; }
    public Double getLongitude() { return this.mLongitude; }

    public void setExpirationTime(Date expiration) { this.mExpirationTime = expiration; }
    public Date getExpirationTime() { return this.mExpirationTime; }

    public String getType() { return this.mType; }
    public void changeType(String type) { this.mType = type; }

    private Arrow(Parcel in) {

        String[] data = new String[3];
        in.readStringArray(data);
        this.mAID = data[0];
        this.mName = data[1];
        this.mMID = data[2];

        this.mExpirationTime = new Date(in.readLong());

        double[] data2 = new double[2];
        in.readDoubleArray(data2);
        this.mLatitude = data2[0];
        this.mLongitude = data2[1];
        this.mDestinationLocation.setLatitude(this.mLatitude);
        this.mDestinationLocation.setLongitude(this.mLongitude);

        this.mType = new String(in.readString());

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeStringArray(new String[]
                {this.mAID, this.mName, this.mMID});
        out.writeLong(mExpirationTime.getTime());
        out.writeDoubleArray(new double[]
                {this.mLatitude, this.mLongitude});
        out.writeString(this.getType());
    }

    public static final Creator<Arrow> CREATOR
            = new Creator<Arrow>() {
        public Arrow createFromParcel(Parcel in) {
            return new Arrow(in);
        }

        public Arrow[] newArray(int size) {
            return new Arrow[size];
        }
    };

}