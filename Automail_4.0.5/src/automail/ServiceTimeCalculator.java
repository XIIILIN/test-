package automail;

public interface ServiceTimeCalculator {
    void doService(int time, RobotType type);

    int getServiceTime(RobotType type);
}
