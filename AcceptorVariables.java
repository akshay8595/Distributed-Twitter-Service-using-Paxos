import javax.persistence.criteria.CriteriaBuilder;
import java.rmi.AccessException;
import java.util.Comparator;

public class AcceptorVariables {

    Integer accNum;
    String accVal;
    Integer slotNumber;

    public void setAccNum(Integer accNum) {
        this.accNum = accNum;
    }

    public Integer getMaxPrepare() {
        return maxPrepare;
    }

    public void setMaxPrepare(Integer maxPrepare) {
        this.maxPrepare = maxPrepare;
    }

    Integer maxPrepare;

    public AcceptorVariables(int accNum, String accVal, Integer slotNumber,int maxPrepare) {
        this.accNum = accNum;
        this.accVal = accVal;
        this.slotNumber = slotNumber;
        this.maxPrepare = maxPrepare;
    }

    public AcceptorVariables(){
        this.accNum = 0;
        this.accVal = "junk";
        this.slotNumber = 0;
        this.maxPrepare = 0;
    }

    public int getAccNum() {
        return accNum;
    }

    public void setAccNum(int accNum) {
        this.accNum = accNum;
    }

    public String getAccVal() {
        return accVal;
    }

    public void setAccVal(String accVal) {
        this.accVal = accVal;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof Event)) {
            return false;
        } else {
            AcceptorVariables acceptorVariable = (AcceptorVariables)obj;
            return this.getSlotNumber().equals(acceptorVariable.getSlotNumber());
        }
    }

    @Override
    public synchronized int hashCode() {
        return this.getSlotNumber().hashCode();
    }

    @Override
    public synchronized String toString() {
        return "AcceptorVariables(" + slotNumber + ", " + accNum + ", "
                + accVal + ")";
    }

}

//Custom comparator that sort based on Event Counter.
//If counter is equal, use timestamp.
class AcceptorVariablesComparator implements Comparator<AcceptorVariables> {
    @Override
    public int compare(AcceptorVariables o1, AcceptorVariables o2) {
        return o1.getSlotNumber().compareTo(o2.getSlotNumber());
    }
}

