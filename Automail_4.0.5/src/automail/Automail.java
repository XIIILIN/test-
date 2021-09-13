package automail;

import simulation.IMailDelivery;

public class Automail {

    private Robot[] robots;
    private MailPool mailPool;

    public Automail(MailPool mailPool, IMailDelivery delivery, int numRegRobots, int numFastRobots, int numBulkRobots) {
        /** Initialize the MailPool */
        this.mailPool = mailPool;

        /** Initialize robots, currently only regular robots */
        robots = new Robot[numRegRobots + numFastRobots + numBulkRobots];
        ServiceTimeCalculator serviceTimeCalculator = new ServiceTimeCalculatorImpl();
        ServiceFeeCalculator serviceFeeCalculator = new ServiceFeeCalculatorImpl(Building.getInstance());
        int j = 0;
        for (int i = 0; i < numRegRobots; i++, j++) robots[j] = new RegularRobot(delivery, mailPool, j, serviceTimeCalculator, serviceFeeCalculator);
        for (int i = 0; i < numFastRobots; i++, j++) robots[j] = new FastRobot(delivery, mailPool, j, serviceTimeCalculator, serviceFeeCalculator);
        for (int i = 0; i < numBulkRobots; i++, j++) robots[j] = new BulkRobot(delivery, mailPool, j, serviceTimeCalculator, serviceFeeCalculator);
    }

    public Robot[] getRobots() {
        return robots;
    }

    public MailPool getMailPool() {
        return mailPool;
    }


}
