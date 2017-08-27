/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.com.gmail.lukacat100.DDNS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ita
 */
public class Profile {
    private String host;
    private String domain;
    private String password;
    public enum Frequency{
        Always(),DayOfWeek(),Repeat();
    }
    private boolean always;
    private List<Timing> timingList;

    public Profile() {
        always = false;
        timingList = new ArrayList<>();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAlways() {
        return always;
    }

    public void setAlways(boolean always) {
        this.always = always;
    }

    public int addTime(Timing time){
        int returned = timingList.size();
        timingList.add(time);
        return returned;
    }
    public Timing getTime(int index){
        return timingList.get(index);
    }
    public void removeTime(int index){
        timingList.remove(index);
    }
    public int getTimeSize(){
        return timingList.size();
    }
    public void runExecutors(){
        for(Timing time : timingList){
            time.startTask(host, domain, password);
        }
    }
    public void stopExecutors(){
        for(Timing time : timingList){
            time.stopTask();
        }
    }
}
