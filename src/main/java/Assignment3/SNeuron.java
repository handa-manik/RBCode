package Assignment3;

import java.util.ArrayList;
import java.util.HashMap;

public class SNeuron {
    static int neuronCounter = 0;
    final public int neuronId;
    NeuronConnection biasConnection;
    final double biasValue = -1;
    double outputValue;
    ArrayList<NeuronConnection> connectionsLeft = new ArrayList<NeuronConnection>();
    HashMap<Integer,NeuronConnection> connectHashMap = new HashMap<Integer,NeuronConnection>();

    public SNeuron(){
        neuronId = neuronCounter;
        neuronCounter++;
    }

    public void add_BiasConnection(SNeuron n){
        NeuronConnection con = new NeuronConnection(n,this);
        biasConnection = con;
        connectionsLeft.add(con);
    }

    public void inputConnectionsAdd(ArrayList<SNeuron> inNeurons){
        for(SNeuron n: inNeurons){
            NeuronConnection con = new NeuronConnection(n,this);
            connectionsLeft.add(con);
            connectHashMap.put(n.neuronId, con);
        }
    }

    public NeuronConnection getConnection(int neuronIndex){
        return connectHashMap.get(neuronIndex);
    }

    public void getOutputValue(int out){
        double s = 0;
        for(NeuronConnection con : connectionsLeft){
            SNeuron leftNeuron = con.getLeftNeuron();
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

    /**
     * This method implements the sigmoid function
     * @param x The input
     * @return f(x) = 1 / (1 + exp(-x))
     */
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
    public ArrayList<NeuronConnection> getInputConnections(){
        return connectionsLeft;
    }
}