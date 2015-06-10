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

package SimulatorsGUI;

import Hack.Utilities.Definitions;
import HackGUI.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * This class represents the GUI of a RAM.
 */
public class RAMComponent extends PointedMemoryComponent {

    // The load file button.
    protected MouseOverJButton loadButton = new MouseOverJButton();
    protected MouseOverJButton saveButton = new MouseOverJButton();

    // The icon on the load file button.
    private ImageIcon loadIcon = new ImageIcon(Utilities.imagesDir + "open2.gif");
    private ImageIcon saveIcon = new ImageIcon(Utilities.imagesDir + "save2.gif");

    // The file filter of this component.
    private FileFilter filter;

    // The file chooser component.
    private JFileChooser fileChooser;

    /**
     * Constructs a new ROMComponent.
     */
    public RAMComponent() {
        filter = new RAMFileFilter();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        jbInit();
    }

    // Initializes this ram.
    private void jbInit()  {

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadButton_actionPerformed(e);
            }
        });
        loadButton.setIcon(loadIcon);
        loadButton.setBounds(new Rectangle(66, 2, 31, 25));
        loadButton.setToolTipText("Load");

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveButton_actionPerformed(e);
            }
        });
        saveButton.setIcon(saveIcon);
        saveButton.setBounds(new Rectangle(97, 2, 31, 25));
        saveButton.setToolTipText("Save");

        this.add(loadButton, null);
        this.add(saveButton, null);
    }

    /**
     * Sets the current working dir.
     */
    public void setWorkingDir(File file) {
        fileChooser.setCurrentDirectory(file);
    }

    /**
     * Opens the file chooser for loading a new program.
     */
    public void loadRAM() {
        int returnVal = fileChooser.showDialog(this, "Load RAM");
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                short[] ram = new short[Definitions.RAM_SIZE];
                Path f = Paths.get(fileChooser.getSelectedFile().getAbsolutePath());
                byte[] bytes = Files.readAllBytes(f);
                if(f.toString().endsWith(".16b")) {
                    // treat every 16bits as a word
                    for(int i = 0, j = 0; i < bytes.length && j < ram.length; i += 2, j++) {
                        if((i+1) < bytes.length) {
                            ram[j] = (short) (((bytes[i] & 0x00FF) << 8) | (bytes[i + 1] & 0x00FF));
                        } else { // odd number of bytes in file, last one gets 0x00 on high bits of word
                            ram[j] = (short)(bytes[i] << 8);
                        }
                    }
                }
                else {
                    // must end in .8b
                    // every 8bits is a word (with 0x00 in the high bits of the word)
                    for(int i = 0; i < bytes.length && i < ram.length; i++) {
                        ram[i] = bytes[i];
                    }
                }
                setContents(ram);
            } catch(Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Implementing the action of pressing the search button.
     */
    public void loadButton_actionPerformed(ActionEvent e) {
        loadRAM();
    }

    /**
     * Opens the file chooser for loading a new program.
     */
    public void saveRAM() {
        int returnVal = fileChooser.showDialog(this, "Save RAM");
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                // ram contents are in `short[] values` (class field)
                // first, find max ram value, which is last position before all 0's for remainder
                // minimum, lastpos will be 1 (to write at least something, even if it's byte 0x0)
                int lastpos = values.length - 1;
                for(; lastpos > 0 && values[lastpos] == 0; lastpos--);
                lastpos++;

                Path f = Paths.get(fileChooser.getSelectedFile().getAbsolutePath());
                FileChannel out = new FileOutputStream(f.toFile()).getChannel();
                if(f.toString().endsWith(".16b")) {
                    // treat every 16bits as a word
                    ByteBuffer myByteBuffer = ByteBuffer.allocate(lastpos*2);
                    myByteBuffer.order(ByteOrder.BIG_ENDIAN);
                    ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
                    short[] valuesTruncated = Arrays.copyOf(values, lastpos);
                    myShortBuffer.put(valuesTruncated);
                    out.write(myByteBuffer);
                }
                else {
                    // must end in .8b
                    // keep only the lower half of every word (e.g., for ASCII text)
                    ByteBuffer myByteBuffer = ByteBuffer.allocate(lastpos);
                    byte[] lowerValues = new byte[lastpos];
                    for(int i = 0; i < lastpos; i++)
                    {
                        lowerValues[i] = (byte) (values[i] & 0x00FF);
                    }
                    myByteBuffer.put(lowerValues);
                    myByteBuffer.rewind();
                    out.write(myByteBuffer);
                }
                out.close();
            } catch(Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Implementing the action of pressing the search button.
     */
    public void saveButton_actionPerformed(ActionEvent e) {
        saveRAM();
    }

}
