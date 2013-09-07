/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pvhs.psibot.custom;

import edu.wpi.first.wpilibj.GenericHID;

/**
 *
 * @author jkoehring
 */
public class Pushbutton
{
	private GenericHID joystick;
	private int buttonNumber;
	private boolean lastState;
	
	public Pushbutton(GenericHID joystick, int buttonNumber)
	{
		this.joystick = joystick;
		this.buttonNumber = buttonNumber;
		lastState = false;
	}
	
	public boolean get()
	{
		return joystick.getRawButton(buttonNumber);
	}
	
	public boolean isDown()
	{
		return get();
	}
	
	/**
	 * Returns true if button was down at last update and is still down.
	 */
	public boolean isHeld()
	{
		return lastState ? get() : false;
	}
	
	/**
	 * Returns true is button has been pressed since last update.
	 */
	public boolean isPressed()
	{
		return lastState ? false : get();
	}
	
	/**
	 * Returns true if button has been releases since last update.
	 */
	public boolean isReleased()
	{
		return lastState ? !get() : false;
	}
	
	public void update()
	{
		lastState = get();
	}
}
