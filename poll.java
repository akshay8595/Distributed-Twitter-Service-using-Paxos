import java.util.ArrayList;

public class poll {

    int MaxSlot;
    UserPaxos PollUser;
    ArrayList<Integer> ReceiveMajority = new ArrayList<>();

    poll(UserPaxos u1)
    {
        PollUser = u1;
    }


    void init(int max)
    {
        MaxSlot = max;
    }

    void send()
    {
        /* iterate through arraylist from PollUser
            and store the majority_promise in ReceiveMajority
         */
    }


}
