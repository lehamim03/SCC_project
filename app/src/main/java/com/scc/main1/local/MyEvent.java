package com.scc.main1.local;

import android.os.Parcel;
import android.os.Parcelable;

public class MyEvent implements Parcelable {
    private String eventTitle;
    private String startDate;
    private String endDate;
    private String eventPlace;
    private int share;

    // 생성자
    public MyEvent(String eventTitle, String startDate, String endDate, String eventPlace, int share) {
        this.eventTitle = eventTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventPlace = eventPlace;
        this.share = share;
    }

    public MyEvent(){
        this.eventTitle = "";
        this.startDate = "";
        this.endDate = "";
        this.eventPlace = "";
        this.share = 0;
    }

    // Parcelable 인터페이스를 구현하는 코드
    protected MyEvent(Parcel in) {
        eventTitle = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        eventPlace = in.readString();
        share = in.readInt();
    }

    public static final Creator<MyEvent> CREATOR = new Creator<MyEvent>() {
        @Override
        public MyEvent createFromParcel(Parcel in) {
            return new MyEvent(in);
        }

        @Override
        public MyEvent[] newArray(int size) {
            return new MyEvent[size];
        }
    };

    // Getter 메서드
    public String getEventTitle() {
        return eventTitle;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getEventPlace() {
        return eventPlace;
    }

    public int getShare() {
        return share;
    }

    // Parcelable 인터페이스의 메서드 구현
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventTitle);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(eventPlace);
        dest.writeInt(share);
    }
}
