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
public class CPUEmulatorConsole
{
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
            final RAM ram = cpu.getRAM();
            short[] ramContents;

            cpuEmulator.setWorkingDir(new File("."));

            ScreenComponent screen = new ScreenComponent();
            short[] screenContents = new short[Definitions.SCREEN_SIZE];

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
                    ram.setValueAt(Definitions.KEYBOARD_ADDRESS, (short)e.getKeyCode(), true);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    ram.setValueAt(Definitions.KEYBOARD_ADDRESS, (short)0, true);
                }
            });

            System.out.println("Loading Hack binary " + args[0]);
            cpuEmulator.doCommand(new String[]{"load", args[0]});
            while(true) {
                // read keyboard

                cpu.executeInstruction();
                // update screen contents by reading RAM
                ramContents = ram.getContents();
                for(int i = Definitions.SCREEN_START_ADDRESS; i < Definitions.SCREEN_END_ADDRESS; i++) {
                    screenContents[(short) (i - Definitions.SCREEN_START_ADDRESS)] = ramContents[i];
                }
                screen.setContents(screenContents);
            }
        }
    }
}
