/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pvhs.psibot.custom;

import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author jkoehring
 */
public class TimedState
{
	private int state;
	private Timer timer;
	private double trigger;
	
	public TimedState(Timer timer, double trigger, int state)
	{
		this.timer = timer;
		this.trigger = trigger = timer.get() + trigger;
		this.state = state;
	}
	
	public boolean check()
	{
		return timer.get() >= trigger;
	}
	
	public int get()
	{
		return state;
	}
}
