package pl.siarko.jetlytra.flight;

public class JetpackPhysicsConfig {

    private double thrustAccel;
    private double thrustAccelDown;
    private double maxThrustVel;
    private double hoverThrustAccel;
    private double hoverThrustMax;
    private double elytraBoostAccel;
    private double elytraBoostMax;
    private double swimBoostMax;
    private double sprintBoostAccel;
    private double sprintBoostMax;

    public double thrustAccel()      { return thrustAccel; }
    public double thrustAccelDown()  { return thrustAccelDown; }
    public double maxThrustVel()     { return maxThrustVel; }
    public double hoverThrustAccel() { return hoverThrustAccel; }
    public double hoverThrustMax()   { return hoverThrustMax; }
    public double elytraBoostAccel() { return elytraBoostAccel; }
    public double elytraBoostMax()   { return elytraBoostMax; }
    public double swimBoostMax()     { return swimBoostMax; }
    public double sprintBoostAccel() { return sprintBoostAccel; }
    public double sprintBoostMax()   { return sprintBoostMax; }

    public void setThrustAccel(double v)      { this.thrustAccel = v; }
    public void setThrustAccelDown(double v)  { this.thrustAccelDown = v; }
    public void setMaxThrustVel(double v)     { this.maxThrustVel = v; }
    public void setHoverThrustAccel(double v) { this.hoverThrustAccel = v; }
    public void setHoverThrustMax(double v)   { this.hoverThrustMax = v; }
    public void setElytraBoostAccel(double v) { this.elytraBoostAccel = v; }
    public void setElytraBoostMax(double v)   { this.elytraBoostMax = v; }
    public void setSwimBoostMax(double v)     { this.swimBoostMax = v; }
    public void setSprintBoostAccel(double v) { this.sprintBoostAccel = v; }
    public void setSprintBoostMax(double v)   { this.sprintBoostMax = v; }
}
