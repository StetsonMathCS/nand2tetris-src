/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

import Hack.CPUEmulator.*;
import Hack.Controller.HackController;
import Hack.Utilities.Definitions;
import SimulatorsGUI.ScreenComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * The CPU Emulator.
 */
public class CPUEmulatorConsole extends JFrame implements Runnable {

    private CPU cpu;
    private RAM ram;
    private ScreenComponent screen;

    public CPUEmulatorConsole(String title, CPU cpu, RAM ram, ScreenComponent screen) {
        super(title);
        this.cpu = cpu;
        this.ram = ram;
        this.screen = screen;
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        setLayout(new BorderLayout());
        add(this.screen, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public synchronized void run() {
        try {
            System.runFinalization();
            System.gc();
            wait(300);
        } catch (InterruptedException ie) {
        }
        int count = 0;
        int rounds = HackController.FASTFORWARD_SPEED_FUNCTION[3];

        while(true) {
            try {
                cpu.executeInstruction();
                notifyAll();
                // waits for 1 ms each constant amount of commands
                if (count == rounds) {
                    count = 0;
                    try {
                        wait(1);
                    } catch (InterruptedException ie) {}
                }
                count++;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MyDispatcher implements KeyEventDispatcher {
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                //System.out.println("keyCode: " + e.getKeyCode());
                short keyCode = 0;
                // translate Java keycode to Hack keycode
                switch(e.getKeyCode()) {
                    case 37: keyCode = 130; break; // left
                    case 38: keyCode = 131; break; // up
                    case 39: keyCode = 132; break; // right
                    case 40: keyCode = 133; break; // down
                    case 8: keyCode = 129; break; // backspace
                    case 10: keyCode = 128; break; // newline
                    // TODO: handle the rest
                    default: keyCode = (short)e.getKeyCode(); break;
                }
                ram.setValueAt(Definitions.KEYBOARD_ADDRESS, (short) keyCode, true);
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                ram.setValueAt(Definitions.KEYBOARD_ADDRESS, (short) 0, true);
            }
            return false;
        }
    }

    /**
     * The command line CPU Emulator program, as a console (no debugging).
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            System.err.println("Usage: java CPUEmulatorConsole [Hack binary]");
        else {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
            }

            CPUEmulator cpuEmulator = new CPUEmulator();
            CPU cpu = cpuEmulator.getCPU();
            cpu.disableAssemblerTranslator();
            RAM ram = cpu.getRAM();

            cpuEmulator.setWorkingDir(new File("."));

            ScreenComponent screen = new ScreenComponent();
            ram.setScreenGUI(screen);

            System.out.println("Loading program " + args[0]);
            cpuEmulator.doCommand(new String[]{"load", args[0]});

            Thread t = new Thread(new CPUEmulatorConsole(args[0], cpu, ram, screen));
            t.start();
        }
    }

}
