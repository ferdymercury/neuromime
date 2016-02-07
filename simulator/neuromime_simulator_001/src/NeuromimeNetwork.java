import processing.core.*;

import java.util.ArrayList;
import processing.event.MouseEvent;

public class NeuromimeNetwork extends PApplet {

	// Stuff for zooming and panning.
	float scaleFactor = 1.0f;
	float translateX = 0.0f;
	float translateY = 0.0f;
	float mouseXSample;
	float mouseYSample;

	boolean freeze = false;

	boolean help = true;
	String helpString;

	// Used to toggle Neurons between "scope" and "blink" modes.
	boolean scope = true;

	// List of all Neurons
	ArrayList<Neuron> neurons = new ArrayList<Neuron>();

	// Single Neuron with an output selected.
	Neuron selectedOutputNeuron;
	// Specific output port of selectedOutputNeuron
	int outputPort;

	public void settings() {
		fullScreen();
	}

	public void setup() {
		background(0);
		helpString = "NOTES\n\n"
				+ "Read all the way through and follow these steps to get started...\n"
				+ "   - left-click the screen to ensure it has the focus\n"
				+ "   - press h to toggle this help text on/off\n"
				+ "   - press n to add a new Neuron (with mouse away from text)\n"
				+ "Green nubs are inputs, white nubs are outputs\n"
				+ "Neurons can be regular neuromimes or signal generators\n"
				+ "   - press s with mouse over Neuron to toggle signal / neuromime mode\n"
				+ "Signal Neurons produce a square wave output and have no inputs\n"
				+ "   - place mouse over signal Neuron dial and left-click to select,\n"
				+ "   then scroll mouse wheel to change frequency\n"
				+ "   - with mouse away from text and Neuron, press n to add another neuron\n"
				+ "   - zoom with mouse wheel and pan by left-click and dragging\n"
				+ "   - to move a Neuron, select it with left-click and then drag\n"
				+ "Left-click any output on the signal Neuron to select it,\n"
				+ "then left-click any input on the new Neuron to make a connection\n"
				+ "The scope inside each Neuron shows the following traces:\n"
				+ "Green trace: input signal      Red trace: membrane voltage\n"
				+ "White trace: output voltage    Grey trace: firing threshold\n"
				+ "   - in neuromime mode, the dial on each neuron is used to adjust firing threshold\n"
				+ "Try lowering the firing threshold on the second Neuron until you see output spikes\n"
				+ "Alternatively, increasing the frequency of the signal neuron will also produce spikes eventually\n"
				+ "   - press b with mouse over Neuron to toggle the visual dispay between \"scope\" or \"blink\" mode\n"
				+ "In blink mode, the Neurons will be white when their output voltage is high, black otherwise\n"
				+ "   - press B to put all Neurons in blink mode\n"
				+ "   - press S to put all Neurons in scope mode\n"
				+ "   - press p to pause / unpause the clock (useful for inspecting traces)\n"
				+ "   - press d with mouse over Neuron to delete it\n"
				+ "   - press N to clear the screen and create 15 new Neurons in a patern\n\n"
				+ "Known Issues\n"
				+ "- Processing is CPU intensive, so your computer will struggle and heat up with lots of Neurons\n"
				+ "- Currently, the simulator works in real-time as per the frame rate, which slows-down when visualizing scopes or\n"
				+ "when there are lots of Neurons. For example, you will notice time-scale jumps in the scope when switching\n"
				+ "between blink and scope modes\n"
				+ "- It is not yet possible to save and reload networks, which sucks... but can be done in the future.\n\n";
	}

	public void draw() {
		// Update all neurons for current timestep.
		if (!freeze) {
			for (int i = 0; i < neurons.size(); i++)
				neurons.get(i).step();
		}
		// Draw all neurons and connections.
		background(30);
		pushMatrix();
		translate(translateX, translateY);
		scale(scaleFactor);
		for (int i = 0; i < neurons.size(); i++)
			neurons.get(i).drawNeuron();
		popMatrix();

		pushMatrix();
		if (help) {
			textSize(14);
			textAlign(LEFT);
			fill(255);
			text(helpString, 10f, -10);
		}
		popMatrix();
	}

	/************************************************************************************************/
	public void mouseWheel(MouseEvent event) {
		for (int i = 0; i < neurons.size(); i++) {
			if (neurons.get(i).thresholdSelected()
					&& mouseOverNeuronDial(neurons.get(i))) {
				if (neurons.get(i).signalNeuron())
					neurons.get(i).freq(
							neurons.get(i).freq() - event.getCount()
									* neurons.get(i).freqInc());
				else
					neurons.get(i).theta(
							neurons.get(i).theta() - event.getCount()
									* neurons.get(i).thetaInc());
				return;
			}
		}

		translateX -= mouseX;
		translateY -= mouseY;
		float delta = event.getCount() < 0 ? 1.05f
				: event.getCount() > 0 ? 1.0f / 1.05f : 1.0f;
		scaleFactor *= delta;
		translateX *= delta;
		translateY *= delta;
		translateX += mouseX;
		translateY += mouseY;
	}

	/************************************************************************************************/
	public void keyPressed() {
		if (key == 'b') {
			for (int i = 0; i < neurons.size(); i++)
				if (mouseOverNeuron(neurons.get(i)))
					neurons.get(i).scope(!neurons.get(i).scope());
		} else if (key == 'B') {
			for (int i = 0; i < neurons.size(); i++)
				neurons.get(i).scope(false);
		} else if (key == 'd') {
			for (int i = 0; i < neurons.size(); i++)
				if (mouseOverNeuron(neurons.get(i)))
					neurons.remove(i);
		} else if (key == 'h') {
			help = !help;
		} else if (key == 'n') {
			neurons.add(new Neuron(this, getMouseX(), getMouseY()));
		} else if (key == 'N') {
			scaleFactor = 0.65f;
			translateX = 186.0f;
			translateY = 127.0f;
			float spc = 400f;
			float midx = width * 0.5f;
			float midy = height * 0.5f;
			neurons.clear();
			neurons.add(new Neuron(this, midx + (-0.5f * spc), midy
					+ (-1.0f * spc)));
			neurons.add(new Neuron(this, midx + (0.5f * spc), midy
					+ (-1.0f * spc)));

			neurons.add(new Neuron(this, midx + (-1.0f * spc), midy
					+ (-0.5f * spc)));
			neurons.add(new Neuron(this, midx + (0.0f * spc), midy
					+ (-0.5f * spc)));
			neurons.add(new Neuron(this, midx + (1.0f * spc), midy
					+ (-0.5f * spc)));

			neurons.add(new Neuron(this, midx + (-1.5f * spc), midy
					+ (0.0f * spc)));
			
			neurons.add(new Neuron(this, midx + (-0.5f * spc), midy
					+ (0.0f * spc)));
			neurons.add(new Neuron(this, midx + (0.5f * spc), midy
					+ (0.0f * spc)));
			neurons.add(new Neuron(this, midx + (1.5f * spc), midy
					+ (0.0f * spc)));

			neurons.add(new Neuron(this, midx + (-1.0f * spc), midy
					+ (0.5f * spc)));
			neurons.add(new Neuron(this, midx + (0.0f * spc), midy
					+ (0.5f * spc)));
			neurons.add(new Neuron(this, midx + (1.0f * spc), midy
					+ (0.5f * spc)));

			neurons.add(new Neuron(this, midx + (-0.5f * spc), midy
					+ (1.0f * spc)));
			neurons.add(new Neuron(this, midx + (0.5f * spc), midy
					+ (1.0f * spc)));
			neurons.get(5).signalNeuron(true);
		} else if (key == 'r') {
			scaleFactor = 1;
			translateX = 0.0f;
			translateY = 0.0f;
		} else if (key == 's') {
			for (int i = 0; i < neurons.size(); i++)
				if (mouseOverNeuron(neurons.get(i)))
					neurons.get(i).signalNeuron(!neurons.get(i).signalNeuron());
		} else if (key == 'p') {
			freeze = !freeze;
		} else if (key == 'S') {
			for (int i = 0; i < neurons.size(); i++)
				neurons.get(i).scope(true);
		}
	}

	/************************************************************************************************/
	public void mousePressed() {
		for (int i = 0; i < neurons.size(); i++) {
			if (mouseOverNeuron(neurons.get(i))) {
				mouseXSample = getMouseX() - neurons.get(i).xPos();
				mouseYSample = getMouseY() - neurons.get(i).yPos();
			}
		}
	}

	/************************************************************************************************/
	public void mouseDragged() {
		// check if drag began over a neuron
		for (int i = 0; i < neurons.size(); i++) {
			if (mouseOverNeuron(neurons.get(i)) && neurons.get(i).isSelected()) {
				neurons.get(i).xPos(getMouseX() - mouseXSample);
				neurons.get(i).yPos(getMouseY() - mouseYSample);
				return;
			}
		}
		translateX += mouseX - pmouseX;
		translateY += mouseY - pmouseY;
	}

	/************************************************************************************************/
	public void mouseClicked() {
		boolean change = false;
		if (mouseButton == LEFT) {
			for (int i = 0; i < neurons.size(); i++) {
				if (mouseOverNeuronDial(neurons.get(i))) {
					neurons.get(i).selectThreshold(
							!neurons.get(i).thresholdSelected());
					if (neurons.get(i).thresholdSelected())
						neurons.get(i).selectNeuron(false);
					change = true;
				} else if (mouseOverNeuron(neurons.get(i))) {
					neurons.get(i).selectNeuron(!neurons.get(i).isSelected());
					if (neurons.get(i).isSelected())
						neurons.get(i).selectThreshold(false);
					change = true;
				} else
					neurons.get(i).selectNeuron(false);
				// connect or disconnect input
				neurons.get(i).selectIn(selectedOutputNeuron, getMouseX(),
						getMouseY());
				// select or de-select output
				if (neurons.get(i).selectOut(getMouseX(), getMouseY())) {
					selectedOutputNeuron = neurons.get(i);
					change = true;
				}
			}
			if (!change) {
				for (int i = 0; i < neurons.size(); i++) {
					neurons.get(i).selectThreshold(false);
					neurons.get(i).clearSelectedOutputs();
				}
			}
		}
	}

	/************************************************************************************************/
	boolean mouseOverNeuron(Neuron n) {
		float x = n.xPos();
		float y = n.yPos();
		float r = n.radius();
		if (sqrt(pow((getMouseX()) - x, 2) + pow((getMouseY()) - y, 2)) <= r)
			return true;
		return false;
	}

	/************************************************************************************************/
	boolean mouseOverNeuronDial(Neuron n) {
		float x = n.xPos();
		float y = n.yPos() + (n.radius() / 2);
		float r = n.dialRadius();
		if (sqrt(pow((getMouseX()) - x, 2) + pow((getMouseY()) - y, 2)) <= r)
			return true;
		return false;
	}

	/************************************************************************************************/
	public float getMouseX() {
		return (mouseX - translateX) / scaleFactor;
	}

	/************************************************************************************************/
	public float getMouseY() {
		return (mouseY - translateY) / scaleFactor;
	}

	/************************************************************************************************/
	public float getX(float x) {
		return (x - translateX) / scaleFactor;
	}

	/************************************************************************************************/
	public float getY(float y) {
		return (y - translateY) / scaleFactor;
	}

	/************************************************************************************************/
	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "NeuromimeNetwork" });
	}
}