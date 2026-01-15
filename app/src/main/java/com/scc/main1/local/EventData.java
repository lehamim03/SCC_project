package com.scc.main1.local;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventData {
    private String codename;
    private String org_name;
    private String date;
    private String guname;
    private String is_free;
    private String place;
    private String ticket;
    private String title;
    private String use_fee;
    private String use_trgt;
    private String startDate;
    private String endDate;
    public EventData() {
        // 기본 생성자
    }
    // Setter 메소드 추가
    public void setCodename(String codename) {
        this.codename = codename;
    }
    public void setOrg_name(String org_name) {this.org_name = org_name; }
    public void setDate(String date) {
        this.date = date;
    }

    public void setGuname(String guname) {
        this.guname = guname;
    }

    public void setIs_free(String is_free) {
        this.is_free = is_free;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUse_fee(String use_fee) {
        this.use_fee = use_fee;
    }

    public void setUse_trgt(String use_trgt) {
        this.use_trgt = use_trgt;
    }

    // Getter 메소드도 필요하다면 추가할 수 있습니다.

    public String getCodename() {
        return codename;
    }
    public String getOrg_name(){return org_name;}
    public String getDate() {
        return date;
    }

    public String getGuname() {
        return guname;
    }

    public String getIs_free() {
        return is_free;
    }

    public String getPlace() {
        return place;
    }

    public String getTicket() {
        return ticket;
    }

    public String getTitle() {
        return title;
    }

    public String getUse_fee() {
        return use_fee;
    }

    public String getUse_trgt() {
        return use_trgt;
    }

    public List<String> getDateList() {
        return Arrays.stream(date.split("~")).collect(Collectors.toList());
    }

    public void setEventTitle(String eventTitle) {
        this.title = eventTitle;
    }
    public void setEventPlace(String eventPlace) {
        this.place = eventPlace;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}


