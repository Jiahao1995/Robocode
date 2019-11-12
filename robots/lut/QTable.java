package lut;

import java.io.*;
import robocode.*;

public class QTable
{
  private double[][] table;

  public QTable()
  {
    table = new double[State.NumStates][Action.NumRobotActions];
    initialize();
  }

  private void initialize()
  {
    for (int i = 0; i < State.NumStates; i++)
      for (int j = 0; j < Action.NumRobotActions; j++)
        table[i][j] = 0.0;
  }

  public double getMaxQValue(int state)
  {
    double maxinum = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < table[state].length; i++)
    {
      if (table[state][i] > maxinum)
        maxinum = table[state][i];
    }
    return maxinum;
  }

  public int getBestAction(int state)
  {
    double maxinum = Double.NEGATIVE_INFINITY;
    int bestAction = 0;
    for (int i = 0; i < table[state].length; i++)
    {
      double qValue = table[state][i];
      //System.out.println("Action " + i + ": " + qValue);
      if (qValue > maxinum)
      {
        maxinum = qValue;
        bestAction = i;
      }
    }
    return bestAction;
  }

  public double getQValue(int state, int action)
  {
    return table[state][action];
  }

  public void setQValue(int state, int action, double value)
  {
    table[state][action] = value;
  }

  public void loadData(File file)
  {
    BufferedReader r = null;
    try
    {
      r = new BufferedReader(new FileReader(file));
      for (int i = 0; i < State.NumStates; i++)
        for (int j = 0; j < Action.NumRobotActions; j++)
          table[i][j] = Double.parseDouble(r.readLine());
    }
    catch (IOException e)
    {
      System.out.println("IOException trying to open reader: " + e);
      initialize();
    }
    catch (NumberFormatException e)
    {
      initialize();
    }
    finally
    {
      try
      {
        if (r != null)
          r.close();
      }
      catch (IOException e)
      {
        System.out.println("IOException trying to close reader: " + e);
      }
    }
  }

  public void saveData(File file)
  {
    PrintStream w = null;
    try
    {
      w = new PrintStream(new RobocodeFileOutputStream(file));
      for (int i = 0; i < State.NumStates; i++)
        for (int j = 0; j < Action.NumRobotActions; j++)
          w.println(new Double(table[i][j]));

      if (w.checkError())
        System.out.println("Could not save the data!");
      w.close();
    }
    catch (IOException e)
    {
      System.out.println("IOException trying to write: " + e);
    }
    finally
    {
      try
      {
        if (w != null)
          w.close();
      }
      catch (Exception e)
      {
        System.out.println("Exception trying to close witer: " + e);
      }
    }
  } 
}