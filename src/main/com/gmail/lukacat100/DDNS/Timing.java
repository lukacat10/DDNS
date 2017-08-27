/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.com.gmail.lukacat100.DDNS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Ita
 */
public class Timing {

    private final static String USER_AGENT = "Mozilla/5.0";

    private final Profile.Frequency type;
    private final Object parameter1, parameter2;
    private ScheduledExecutorService executorService;

    public Timing(Profile.Frequency frequencyType, Object parameter1, Object parameter2) throws Exception {
        if (frequencyType == Profile.Frequency.Always) {
            throw new Exception("Wrong operation, shouldn't happen!");
        }
        this.type = frequencyType;
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        executorService = new ScheduledThreadPoolExecutor(1);
    }

    public Profile.Frequency getFrequencyType() {
        return type;
    }

    public Object getParameter1() {
        return parameter1;
    }

    public Object getParameter2() {
        return parameter2;
    }

    /*WARNING! NASTY CODE IN THE METOHD BELOW! 
    This method contains unreadable piece of code and should not be 
    read by anyone until I redo it to at least look normal. Don't blame me,
    nobody sees that code anyway :)
     */
    private long timeToNextRun() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow;

        /*Parameters provided by user first converted from objects to strings, 
        for extra processing (time will later become ints).
         */
        String dow = ((String) parameter1);
        String time = ((String) parameter2);

        //DayOfWeek.valueOf("".toUpperCase()).getValue();
        if (this.type == Profile.Frequency.DayOfWeek) {
            int hours;
            int minutes;

            hours = Integer.parseInt(time.substring(0, 2));//11:00pm 7
            minutes = Integer.parseInt(time.substring(3, 5));

            if (time.contains("pm")) {
                if (hours != 12) {
                    hours += 12;
                }
            }
            if (time.contains("am") && hours == 12) {
                hours = 0;
            }

            zonedNextTarget = zonedNow.withHour(hours).withMinute(minutes);
            if (dow.equals("Everyday")) {
                if (zonedNextTarget.isBefore(zonedNow)) {
                    zonedNextTarget = zonedNextTarget.plusDays(1);
                    ////////////////////////////return;
                }
            } else {
                DayOfWeek day = DayOfWeek.valueOf(dow.toUpperCase());
                if (day.getValue() == zonedNow.getDayOfWeek().getValue()) {
                    if (zonedNextTarget.isBefore(zonedNow)) {
                        zonedNextTarget = zonedNextTarget.plusDays(7);
                        ////////////////////////////return;
                    }
                } else {
                    int timeToSunday = 7 - zonedNow.getDayOfWeek().getValue();
                    if (day.getValue() < zonedNow.getDayOfWeek().getValue()) {
                        zonedNextTarget = zonedNextTarget.plusDays(DayOfWeek.SUNDAY.getValue() + (day.getValue() - timeToSunday));
                    } else {
                        zonedNextTarget = zonedNextTarget.plusDays(day.getValue() - zonedNow.getDayOfWeek().getValue());
                    }
                    ////////////////////////////return;
                }
            }
        }
        if (this.type == Profile.Frequency.Repeat) {
            int amount = Integer.parseInt((String) parameter1);
            String units = (String) parameter2;
            switch (units) {
                case "Months":
                    zonedNextTarget = zonedNextTarget.plusMonths(amount);
                    break;
                case "Weeks":
                    zonedNextTarget = zonedNextTarget.plusWeeks(amount);
                    break;
                case "Days":
                    zonedNextTarget = zonedNextTarget.plusDays(amount);
                    break;
                case "Hours":
                    zonedNextTarget = zonedNextTarget.plusHours(amount);
                    break;
                case "Minutes":
                    zonedNextTarget = zonedNextTarget.plusMinutes(amount);
                    break;
                case "Seconds":
                    zonedNextTarget = zonedNextTarget.plusSeconds(amount);
                    break;
            }
        }
        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        return duration.getSeconds();
    }

    //!TODO! - this method is still copypasta!
    private boolean sendGetRequest(String host, String domain, String password) throws MalformedURLException, ProtocolException, IOException {
        if (MainUI.debug) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Sent a check!");
                }
            };
            Thread thread = new Thread(run);
            thread.start();
        }
        String url = "https://dynamicdns.park-your-domain.com/update?host=" + host + "&domain=" + domain + "&password=" + password + "";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'GET' request to URL : " + url);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //JOptionPane.showMessageDialog(null, "pupik");
        //Thread.sleep(60000);
        //print result
        //System.out.println(response.toString());
        return false;//!TODO! - check if response says things went well...
    }

    public void startTask(String host, String domain, String password) {
        Runnable taskWrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = false;
                    sendGetRequest(host, domain, password);
                    /*
                        !TODO! - after success check is implemented, ui should 
                        update accordingly (status list).
                     */
                    startTask(host, domain, password);
                } catch (ProtocolException ex) {
                    Logger.getLogger(Timing.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Timing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        long delay = timeToNextRun();
        executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
        if (MainUI.debug) {
            JOptionPane.showMessageDialog(null, "Generated delay: " + delay);
        }
    }

    public void stopTask() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    @Override
    public String toString() {
        String returned = "";
        if (this.type == Profile.Frequency.DayOfWeek) {
            returned = parameter1 + " at " + parameter2;
        } else {
            LocalDateTime localNow = LocalDateTime.now();
            ZoneId currentZone = ZoneId.systemDefault();
            ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
            returned = "Every " + parameter1 + " " + parameter2 + "(Created: " + zonedNow.format(DateTimeFormatter.RFC_1123_DATE_TIME) + ")";
        }
        return returned; //To change body of generated methods, choose Tools | Templates.
    }

}
