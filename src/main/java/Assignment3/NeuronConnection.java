package Assignment3;

public class NeuronConnection {

    double weightValue = 0;
    double deltaPreviousWeight = 0;
    double deltaWeight = 0;
    final SNeuron LNeuron;
    static int connectionCounter = 0;
    final public int connectionId;

    public NeuronConnection(SNeuron fromN, SNeuron toN) {
        LNeuron = fromN;
        connectionId = connectionCounter;
        connectionCounter++;
    }

    // update deltaweight and prevdelta weight during backprop
    public void calculateWeightDelta(double w) {
        deltaPreviousWeight = deltaWeight;
        deltaWeight = w;
    }

    public double getPreviousDeltaWeight() {
        return deltaPreviousWeight;
    }

    public SNeuron getLeftNeuron() {
        return LNeuron;
    }

    public void updatedWeight(double w) {
        weightValue = w;
    }

    public double getWeightValue() {
        return weightValue;
    }
}
