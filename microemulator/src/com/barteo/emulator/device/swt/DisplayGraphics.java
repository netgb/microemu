/*
 *  MicroEmulator
 *  Copyright (C) 2001 Bartek Teodorczyk <barteo@it.pl>
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
 *
 *  Contributor(s):
 *    3GLab
 */
 
package com.barteo.emulator.device.swt;

import javax.microedition.lcdui.Image;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import com.barteo.emulator.app.ui.swt.SwtDeviceComponent;
import com.barteo.emulator.device.DeviceDisplay;
import com.barteo.emulator.device.DeviceFactory;


public class DisplayGraphics extends javax.microedition.lcdui.Graphics 
{
  GC g;
  int color = 0;
  javax.microedition.lcdui.Font currentFont = javax.microedition.lcdui.Font.getDefaultFont();
  

// to zostanie zmienione na protected jak zostanie zrobiony DisplayBridge
  public DisplayGraphics(GC a_g) 
  {
    g = a_g;
    g.setBackground(((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getBackgroundColor());
		// TODO poprawic setFont	
//    g.setFont(
//        ((SwtFontManager) DeviceFactory.getDevice().getFontManager()).getFontMetrics(currentFont).getFont());
  }


  public int getColor()
  {
    return color;
  }

  
  public void setColor(int RGB) 
  {
		java.awt.image.RGBImageFilter filter = null;
    color = RGB;
    
    DeviceDisplay deviceDisplay = DeviceFactory.getDevice().getDeviceDisplay();
    if (deviceDisplay.isColor()) {
			filter = new RGBImageFilter();
    } else {
      if (deviceDisplay.numColors() == 2) {
        filter = new BWImageFilter();
      } else {
        filter = new GrayImageFilter();
      }
    }

    g.setForeground(SwtDeviceComponent.createColor(filter.filterRGB(0, 0, RGB)));
  }


	public javax.microedition.lcdui.Font getFont()
	{
		return currentFont;
	}


	public void setFont(javax.microedition.lcdui.Font font)
	{
		currentFont = font;
		// TODO poprawic setFont	
//    g.setFont(
//        ((SwtFontManager) DeviceFactory.getDevice().getFontManager()).getFontMetrics(currentFont).getFont());
	}


  public void clipRect(int x, int y, int width, int height) 
  {
		// TODO poprawic clipRect	
//    g.clipRect(x, y, width, height);
  }


  public void setClip(int x, int y, int width, int height) 
  {
    g.setClipping(x + translateX, y + translateY, width, height);
  }


  public int getClipX()
  {
    Rectangle rect = g.getClipping();
    if (rect == null) {
      return 0;
    } else {
      return rect.x;
    }
  }


  public int getClipY()
  {
    Rectangle rect = g.getClipping();
    if (rect == null) {
      return 0;
    } else {
      return rect.y;
    }
  }


  public int getClipHeight()
  {
    Rectangle rect = g.getClipping();
    if (rect == null) {
      return DeviceFactory.getDevice().getDeviceDisplay().getHeight();
    } else {
      return rect.height;
    }
  }


  public int getClipWidth()
  {
    Rectangle rect = g.getClipping();
    if (rect == null) {
      return DeviceFactory.getDevice().getDeviceDisplay().getWidth();
    } else {
      return rect.width;
    }
  }


  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) 
  {
    g.drawArc(x + translateX, y + translateY, width, height, startAngle, arcAngle);
  }


  public void drawImage(Image img, int x, int y, int anchor) 
  {
    int newx = x + translateX;
    int newy = y + translateY;

    if (anchor == 0) {
      anchor = javax.microedition.lcdui.Graphics.TOP | javax.microedition.lcdui.Graphics.LEFT;
    }

    if ((anchor & javax.microedition.lcdui.Graphics.RIGHT) != 0) {
      newx -= img.getWidth();
    } else if ((anchor & javax.microedition.lcdui.Graphics.HCENTER) != 0) {
      newx -= img.getWidth() / 2;
    }
    if ((anchor & javax.microedition.lcdui.Graphics.BOTTOM) != 0) {
      newy -= img.getHeight();
    } else if ((anchor & javax.microedition.lcdui.Graphics.VCENTER) != 0) {
      newy -= img.getHeight() / 2;
    }

    if (img.isMutable()) {
      g.drawImage(((MutableImage) img).getImage(), newx, newy);
    } else {
      g.drawImage(((ImmutableImage) img).getImage(), newx, newy);
    }
  }


  public void drawLine(int x1, int y1, int x2, int y2) 
  {
    g.drawLine(x1 + translateX, y1 + translateY, x2 + translateX, y2 + translateY);
  }


  public void drawRect(int x, int y, int width, int height) 
  {
    drawLine(x + translateX, y + translateY, x + width + translateX, y + translateY);
    drawLine(x + width + translateX, y + translateY, x + width + translateX, y + height + translateY);
    drawLine(x + width + translateX, y + height + translateY, x + translateX, y + height + translateY);
    drawLine(x + translateX, y + height + translateY, x + translateX, y + translateY);
  }


  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) 
  {
    g.drawRoundRectangle(x + translateX, y + translateY, width, height, arcWidth, arcHeight);
  }


  public void drawString(String str, int x, int y, int anchor) 
  {
    int newx = x + translateX;
    int newy = y + translateY;

    if (anchor == 0) {
      anchor = javax.microedition.lcdui.Graphics.TOP | javax.microedition.lcdui.Graphics.LEFT;
    }

    if ((anchor & javax.microedition.lcdui.Graphics.VCENTER) != 0) {
      newy -= g.getFontMetrics().getAscent();
    } else if ((anchor & javax.microedition.lcdui.Graphics.BOTTOM) != 0) {
      newy -= g.getFontMetrics().getHeight();
    }
    if ((anchor & javax.microedition.lcdui.Graphics.HCENTER) != 0) {
      newx -= g.stringExtent(str).x / 2;
    } else if ((anchor & javax.microedition.lcdui.Graphics.RIGHT) != 0) {
      newx -= g.stringExtent(str).x;
    }

    g.drawString(str, newx, newy, true);
    
    if ((currentFont.getStyle() & javax.microedition.lcdui.Font.STYLE_UNDERLINED) != 0) {
      g.drawLine(newx, newy + 1, newx + g.stringExtent(str).x, newy + 1);
    }
  }


  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) 
  {
    g.fillArc(x + translateX, y + translateY, width, height, startAngle, arcAngle);
  }


  public void fillRect(int x, int y, int width, int height) 
  {
		Color tmp = g.getBackground();
		g.setBackground(g.getForeground());
    g.fillRectangle(x + translateX, y + translateY, width, height);
    g.setBackground(tmp);
  }


  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) 
  {
    g.fillRoundRectangle(x + translateX, y + translateY, width, height, arcWidth, arcHeight);
  }

}