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
public class CPUEmulatorConsole implements Runnable {

    private CPU cpu;

    public CPUEmulatorConsole(CPU cpu) {
        this.cpu = cpu;
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

    /**
     * The command line CPU Emulator program, as a console (no debugging).
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            System.err.println("Usage: java CPUEmulatorConsole [Hack binary]");
        else {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
            }

            CPUEmulator cpuEmulator = new CPUEmulator();
            CPU cpu = cpuEmulator.getCPU();
            cpu.disableAssemblerTranslator();
            final RAM ram = cpu.getRAM();

            cpuEmulator.setWorkingDir(new File("."));

            ScreenComponent screen = new ScreenComponent();
            ram.setScreenGUI(screen);

            JFrame frame = new JFrame(args[0]);
            frame.setLayout(new BorderLayout());
            frame.add(screen, BorderLayout.CENTER);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            frame.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    // do nothing
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    ram.setValueAt(Definitions.KEYBOARD_ADDRESS, (short) e.getKeyCode(), true);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    ram.setValueAt(Definitions.KEYBOARD_ADDRESS, (short) 0, true);
                }
            });

            System.out.println("Loading program " + args[0]);
            cpuEmulator.doCommand(new String[]{"load", args[0]});


            Thread t = new Thread(new CPUEmulatorConsole(cpu));
            t.start();
        }
    }

}
