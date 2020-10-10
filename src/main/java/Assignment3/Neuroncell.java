
package Assignment3;

import java.util.*;

public class Neuroncell {

   /* static int neuronCounter = 0;
    final public int neuronId;
    Connect biasConnection;
    final double biasValue = -1;
    double outputValue;
    ArrayList<Connect> connectionsLeft = new ArrayList<Connect>();
    HashMap<Integer,Connect> connectHashMap = new HashMap<Integer,Connect>();

    public Neuroncell(){
        neuronId = neuronCounter;
        neuronCounter++;
    }

    public void add_BiasConnection(Neuroncell n){
        Connect con = new Connect(n,this);
        biasConnection = con;
        connectionsLeft.add(con);
    }

    public void inputConnectionsAdd(ArrayList<Neuroncell> inNeurons){
        for(Neuroncell n: inNeurons){
            Connect con = new Connect(n,this);
            connectionsLeft.add(con);
            connectHashMap.put(n.neuronId, con);
        }
    }

    public Connect getConnection(int neuronIndex){
        return connectHashMap.get(neuronIndex);
    }

    public void getOutputValue(int out){
        double s = 0;
        for(Connect con : connectionsLeft){
            Neuroncell leftNeuron = con.getLeftNeuron();
            double weight = con.getWeightValue();
            double acc = leftNeuron.getOutputValue();

            s = s + (weight*acc);
        }
        s = s + (biasConnection.getWeightValue()*biasValue);

        if (out == 1){
            outputValue = s;
        }
        else {
            outputValue = sigmoid(s);
        }
    }

    double bipolarSigmoid(double x) {
        return (1.0 - (Math.exp(-x)))/ (1.0 +  (Math.exp(-x)));
    }

    *//**
     * This method implements the sigmoid function
     * @param x The input
     * @return f(x) = 1 / (1 + exp(-x))
     *//*
    public double sigmoid(double x)
    {
        double result;
        result =  1 / (1 + Math.exp(-x));
        return result;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public void setOutput(double o){
        outputValue = o;
    }

    public double getBias() {
        return biasValue;
    }

    // get all the connections coming from the previous layer  
    public ArrayList<Connect> getInputConnections(){
        return connectionsLeft;
    }
*/

}