package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.IMailDelivery;
import util.Configuration;

public class BulkRobot extends Robot {
    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     * @param number
     */
    public BulkRobot(IMailDelivery delivery, MailPool mailPool, int number, ServiceTimeCalculator serviceTimeCalculator, ServiceFeeCalculator serviceFeeCalculator) {
        super(delivery, mailPool, number, serviceTimeCalculator, serviceFeeCalculator);
        id = "B" + id.substring(1);
        robot_type = RobotType.BULK;
    }

    @Override
    public String getIdTube() {
        return String.format("%s(%1d)", this.id, (tube.size() + (deliveryItem != null ? 1 : 0)));
    }

    @Override
    public void operate() throws ExcessiveDeliveryException {
        super.operate();
    }

    @Override
    public void doDelivery() throws ExcessiveDeliveryException {
        Configuration configuration = Configuration.getInstance();
        int num_bulk_robots = Integer.parseInt(configuration.getProperty(Configuration.BULK_ROBOTS_KEY));
        boolean feeCharge = Boolean.parseBoolean(configuration.getProperty(Configuration.FEE_CHARGING_KEY));
        String extra = "";

        if (feeCharge) {
            double serviceFee = serviceFeeCalculator.calculateServiceFee(deliveryItem.getDestFloor());
            double avgOperatingTime = serviceTimeCalculator.getServiceTime(this.robot_type) * 1.0 / num_bulk_robots;

            double maintenance = getMaintenanceRate() * avgOperatingTime;
            double totalCharge = serviceFee + maintenance;
            extra = String.format(" | Service Fee: %.2f | Maintenance: %.2f | Avg. Operating Time: %.2f | Total Charge: %.2f",
                    serviceFee, maintenance, avgOperatingTime, totalCharge);
        }

        /** Check if want to return, i.e. if there is no item in the tube*/
        if (deliveryItem != null) {
            MailItem item = deliveryItem;
            deliveryItem = null;
            delivery.deliver(this, item, extra);
            deliveryCounter++;
            if (tube.size() > 0) {
                deliveryItem = tube.remove(tube.size() - 1);
                setDestination();
                changeState(RobotState.DELIVERING);
            } else {
                changeState(RobotState.RETURNING);
            }
        } else {
            changeState(RobotState.RETURNING);
        }
    }

    @Override
    protected void moveTowards(int destination) {
        super.moveTowards(destination);
    }

    public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
//        assert (deliveryItem == null);
//        if (getTube().size() < 5) {
//            super.addToTube(mailItem);
//            deliveryItem = mailItem;
//        }
//        if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        throw new ItemTooHeavyException();
    }


    @Override
    public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
        if (deliveryItem == null) {
            deliveryItem = mailItem;
        } else {
            if (getTube().size() < 4) {
                getTube().add(deliveryItem);
                deliveryItem = mailItem;
            } else {
                throw new ItemTooHeavyException();
            }
        }
    }

    @Override
    public double getMaintenanceRate() {
        return 0.01;
    }


}
