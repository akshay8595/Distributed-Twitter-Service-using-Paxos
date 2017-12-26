
public class proposer
{
    int proposalNumber;
    int proposalIncrement;
    boolean majority = false;
    /*
    Initialise an array of object which Json uses to receive consensus on the majorite of value V

     */
    boolean increment;
    int prepareMessage=0;
    void proposer()
    {
        proposalNumber=1;
        proposalIncrement=5;
        increment = false;
    }

    public void prepare()
    {
        if(increment)
        {
            proposalNumber += proposalIncrement;
        }
        /*
        call a function write with the prepare message with flag prepareMessage set to 1.
         */
    }
    public void receivePromise(int accNum, String accVal)
    {
        if(accNum>0)
            majority = true;
        if(accVal !="$")
            majority = true;

        /*add all the receipts of Promise to an array list of JSON objects */
    }
    public void sendAccept()
    {
        /*
            function to write proposal number and value to the Acceptor socket
             */
        if(majority){

        }
         /*
            function to write proposal number and value with its own proposal number to the Acceptor Socket
             */
        else{

        }
    }

}