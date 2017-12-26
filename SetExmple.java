
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ananya
 */
public class SetExmple {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Dictionary dict = new Dictionary();

        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        long epochMillis = utc.toEpochSecond() * 1000;
        System.out.println(epochMillis);
        System.out.println(Event.OperationTypes.SEND);
    }

}
