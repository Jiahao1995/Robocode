package lut;

public class Learner
{
  public static final double LearningRate = 0.1;
  public static final double DiscountRate = 0.9;
  public static final double ExploitationRate = 1;
  private int lastState;
  private int lastAction;
  private boolean first = true;
  private QTable table;

  public Learner(QTable table)
  {
    this.table = table;
  }

  public void learn(int state, int action, double reinforcement)
  {
    System.out.println("Reinforcement: " + reinforcement);
    if (first)
      first = false;
    else
    {
      double oldQValue = table.getQValue(lastState, lastAction);
      double newQValue = (1 - LearningRate) * oldQValue + LearningRate * (reinforcement + DiscountRate * table.getMaxQValue(state));
      System.out.println("Old Q-Value: " + oldQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - oldQValue));
      table.setQValue(lastState, lastAction, newQValue);
    }
    lastState = state;
    lastAction = action;
  }

  public int selectAction(int state, long time)
  {
    double qValue;
    double sum = 0.0;
    double[] value = new double[Action.NumRobotActions];
    for (int i = 0; i < value.length; i++)
    {
      qValue = table.getQValue(state, i);
      value[i] = Math.exp(ExploitationRate * qValue);
      sum += value[i];
      System.out.println("Q-value: " + qValue);
    }

    if (sum != 0)
      for (int i = 0; i < value.length; i++)
      {
        value[i] /= sum;
        System.out.println("P(a|s): " + value[i]);
      }
    else
      return table.getBestAction(state);

    int action = 0;
    double cumProb = 0.0;
    double randomNum = Math.random();
    System.out.println("Random Number: " + randomNum);
    while (randomNum > cumProb && action < value.length)
    {
      cumProb += value[action];
      action++;
    }
    return action - 1;
  } 
}