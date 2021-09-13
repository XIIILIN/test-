package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.IMailDelivery;
import util.Configuration;

public class FastRobot extends Robot {
    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     * @param number
     */
    public FastRobot(IMailDelivery delivery, MailPool mailPool, int number, ServiceTimeCalculator serviceTimeCalculator, ServiceFeeCalculator serviceFeeCalculator) {
        super(delivery, mailPool, number, serviceTimeCalculator, serviceFeeCalculator);
        id = "F" + id.substring(1);
        robot_type = RobotType.FAST;
    }

    @Override
    public void doDelivery() throws ExcessiveDeliveryException {
        Configuration configuration = Configuration.getInstance();
        boolean feeCharge = Boolean.parseBoolean(configuration.getProperty(Configuration.FEE_CHARGING_KEY));
        String extra = "";
        if(feeCharge) {
            int num_fast_robots = Integer.parseInt(configuration.getProperty(Configuration.FAST_ROBOTS_KEY));
            double serviceFee = serviceFeeCalculator.calculateServiceFee(deliveryItem.getDestFloor());
            double avgOperatingTime = serviceTimeCalculator.getServiceTime(this.robot_type) * 1.0 / num_fast_robots;

            double maintenance = getMaintenanceRate() * avgOperatingTime;
            double totalCharge = serviceFee + maintenance;
            extra = String.format(" | Service Fee: %.2f | Maintenance: %.2f | Avg. Operating Time: %.2f | Total Charge: %.2f",
                    serviceFee, maintenance, avgOperatingTime, totalCharge);
        }
        delivery.deliver(this, deliveryItem, extra);
        deliveryItem = null;
        deliveryCounter++;
        /** Check if want to return, i.e. if there is no item in the tube*/
        changeState(RobotState.RETURNING);
    }


    @Override
    protected void moveTowards(int destination) {
        for (int i = 0; i < 3; i++) {
            if (current_floor == destination) {
                break;
            }
            super.moveTowards(destination);
        }
    }

    @Override
    public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
        throw new ItemTooHeavyException();
    }

    @Override
    public void operate() throws ExcessiveDeliveryException {
        super.operate();
    }

    @Override
    public double getMaintenanceRate() {
        return 0.05;
    }

}
