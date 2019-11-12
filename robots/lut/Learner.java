package lut;

public class Learner {
    public static final double LEARNING_RATE = 0.1;
    public static final double DISCOUNT_FACTOR = 0.9;
    public static final double EXPLOITATION_RATE = 1;
    private int prevState;
    private int prevAction;
    private boolean isFirst = true;
    private LUT lut;

    public Learner(LUT lut) {
        this.lut = lut;
    }

    public void offPolicyLearn(int state, int action, double reinforcement) {
        if (isFirst)
            isFirst = false;
        else {
            double oldQValue = lut.getQValue(prevState, prevAction);
            double newQValue = (1 - LEARNING_RATE) * oldQValue + LEARNING_RATE * (reinforcement + DISCOUNT_FACTOR * lut.getMaxQValue(state));
//            System.out.println("Old Q-Value: " + oldQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - oldQValue));
            lut.setQValue(prevState, prevAction, newQValue);
        }
        prevState = state;
        prevAction = action;
    }

    public void onPolicyLearn(int state, int action, double reinforcement) {
        if (isFirst)
            isFirst = false;
        else {
            double oldQValue = lut.getQValue(prevState, prevAction);
            double newQValue = (1 - LEARNING_RATE) * oldQValue + LEARNING_RATE * (reinforcement + DISCOUNT_FACTOR * lut.getQValue(state, action));
//            System.out.println("Old Q-Value: " + oldQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - oldQValue));
            lut.setQValue(prevState, prevAction, newQValue);
        }
        prevState = state;
        prevAction = action;
    }

    public int selectAction(int state, long time) {
        double qValue;
        double sum = 0.0;
        double[] value = new double[Action.NUM_ROBOT_ACTIONS];
        for (int i = 0; i < value.length; i++) {
            qValue = lut.getQValue(state, i);
            value[i] = Math.exp(EXPLOITATION_RATE * qValue);
            sum += value[i];
            System.out.println("Q-value: " + qValue);
        }

        if (sum != 0)
            for (int i = 0; i < value.length; i++) {
                value[i] /= sum;
                System.out.println("P(a|s): " + value[i]);
            }
        else
            return lut.getBestAction(state);

        int action = 0;
        double cumProb = 0.0;
        double randomNum = Math.random();
        System.out.println("Random Number: " + randomNum);
        while (randomNum > cumProb && action < value.length) {
            cumProb += value[action];
            action++;
        }
        return action - 1;
    }
}
