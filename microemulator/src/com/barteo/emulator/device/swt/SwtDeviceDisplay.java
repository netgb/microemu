/*
 *  MicroEmulator
 *  Copyright (C) 2002 Bartek Teodorczyk <barteo@it.pl>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package com.barteo.emulator.device.swt;

import java.util.Enumeration;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import com.barteo.emulator.EmulatorContext;
import com.barteo.emulator.MIDletBridge;
import com.barteo.emulator.device.Device;
import com.barteo.emulator.device.DeviceDisplay;
import com.barteo.emulator.device.DeviceFactory;
import com.barteo.emulator.device.InputMethod;


public class SwtDeviceDisplay implements DeviceDisplay
{
  EmulatorContext context;

  Rectangle displayRectangle;
  Rectangle displayPaintable;
    
  boolean isColor;
  int numColors;
  Color backgroundColor;
  Color foregroundColor;
  
  PositionedImage upImage;
  PositionedImage downImage;
  
  PositionedImage mode123Image;  
  PositionedImage modeAbcUpperImage;  
  PositionedImage modeAbcLowerImage;
  
  boolean scrollUp = false;
  boolean scrollDown = false;

  
  SwtDeviceDisplay(EmulatorContext acontext)
  {
    context = acontext; 
  }
  
  
  public EmulatorContext getEmulatorContext()
  {
    return context;
  }

  
  public int getHeight()
  {
    return displayPaintable.height;
  }
  
  
  public int getWidth()
  {
    return displayPaintable.width;
  }
  
  
  public boolean isColor()
  {
    return isColor;
  }
  
  
  public int numColors()
  {
    return numColors;
  }
  
  
  public void paint(GC g)
  {
    Device device = DeviceFactory.getDevice();

    g.setForeground(backgroundColor);    
    g.fillRectangle(0, 0, displayRectangle.width, displayPaintable.y);
    g.fillRectangle(0, displayPaintable.y, displayPaintable.x, displayPaintable.height);
    g.fillRectangle(
    		displayPaintable.x + displayPaintable.width, displayPaintable.y, 
    		displayRectangle.width - displayPaintable.x - displayPaintable.width, displayPaintable.height);
    g.fillRectangle(
    		0, displayPaintable.y + displayPaintable.height, 
    		displayRectangle.width, displayRectangle.height - displayPaintable.y - displayPaintable.height);		

    g.setForeground(foregroundColor);
    for (Enumeration s = device.getSoftButtons().elements(); s.hasMoreElements(); ) {
      ((SwtSoftButton) s.nextElement()).paint(g);
    }

    int inputMode = device.getInputMethod().getInputMode();
    if (inputMode == InputMethod.INPUT_123) {
      g.drawImage(mode123Image.getImage(), 
          mode123Image.getRectangle().x, mode123Image.getRectangle().y);
    } else if (inputMode == InputMethod.INPUT_ABC_UPPER) {
      g.drawImage(modeAbcUpperImage.getImage(), 
          modeAbcUpperImage.getRectangle().x, modeAbcUpperImage.getRectangle().y);
    } else if (inputMode == InputMethod.INPUT_ABC_LOWER) {
      g.drawImage(modeAbcLowerImage.getImage(), 
          modeAbcLowerImage.getRectangle().x, modeAbcLowerImage.getRectangle().y);
    }

    Rectangle oldclip = g.getClipping();
    g.setClipping(displayPaintable);
		// TODO poprawic translate	
//    g.translate(displayPaintable.x, displayPaintable.y);
    Font f = g.getFont();
    
    DisplayGraphics dg = new DisplayGraphics(g);
    MIDletBridge.getMIDletAccess().getDisplayAccess().paint(dg);
    
    g.setFont(f);
		// TODO poprawic translate	
//    g.translate(-displayPaintable.x, -displayPaintable.y);
    g.setClipping(oldclip);

    if (scrollUp) {
      g.drawImage(upImage.getImage(), upImage.getRectangle().x, upImage.getRectangle().y);
    }
    if (scrollDown) {
      g.drawImage(downImage.getImage(), downImage.getRectangle().x, downImage.getRectangle().y);
    }
  }
  
  
  public void repaint()
  {
    context.getDisplayComponent().repaint();
  }
  
  
  public void setScrollDown(boolean state) 
  {
    scrollDown = state;
  }


  public void setScrollUp(boolean state) 
  {
    scrollUp = state;
  }

    
  public Rectangle getDisplayRectangle()
  {
    return displayRectangle;
  }


  public Color getBackgroundColor()
  {
    return backgroundColor;
  }

  
  public Color getForegroundColor()
  {
    return foregroundColor;
  }
    
}