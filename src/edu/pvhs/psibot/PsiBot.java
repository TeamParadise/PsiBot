/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.pvhs.psibot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.pvhs.psibot.custom.Pushbutton;
import edu.pvhs.psibot.custom.TimedState;

import edu.wpi.first.wpilibj.SimpleRobot;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class PsiBot extends SimpleRobot
{
	Timer initTimer = new Timer();
	
    RobotDrive chassis = new RobotDrive(new Talon(1), new Talon(2), new Talon(3), new Talon(4));
	
    Joystick stick1 = new Joystick(1);
    Joystick stick2 = new Joystick(2);

	Pushbutton buttonFeed = new Pushbutton(stick1, 2);
	Pushbutton buttonShoot = new Pushbutton(stick1, 1);
	Pushbutton buttonRelease = new Pushbutton(stick2, 1);
	Pushbutton buttonRelease2 = new Pushbutton(stick2, 2);
		
	Victor winch = new Victor(9);
	Victor shootFront = new Victor(6);
	Victor shootRear = new Victor(7);
	
	// SERVOS NEED A JUMPER BY THE PWM
	Servo feed = new Servo(8);
	Servo release = new Servo(10);
	
	Relay back = new Relay(1);
	
	DigitalInput flagPoleLeft = new DigitalInput(1);
	DigitalInput flagPoleRight = new DigitalInput(2);
	DigitalInput winchBarLeft = new DigitalInput(5);
	DigitalInput winchBarRight = new DigitalInput(6);
	DigitalInput winchSpool = new DigitalInput(7);
	DigitalInput winchUnwound = new DigitalInput(8);
	    
    double calX;
    double calY;
    double calTwist;
    double calGyroAngle = 0;
    double calWinch;
	
	double frontShooterSpeed;
	double frontMinShooterSpeed;
	double rearShooterSpeed;
	double rearMinShooterSpeed;
	
	public PsiBot()
	{
		initTimer.start();
		
		frontShooterSpeed = 1.0;
		frontMinShooterSpeed = 0.20;
		rearShooterSpeed = 1.0;
		rearMinShooterSpeed = 0.20;
		
		SmartDashboard.putNumber("Shooter Power", frontShooterSpeed);
		SmartDashboard.putNumber("Continuous Fire Delay", 0.5);
		SmartDashboard.putNumber("Continuous Feed Delay", 0.5);
		SmartDashboard.putNumber("Release Active State", 180.0);
		SmartDashboard.putNumber("Release Default State", 0.0);
		
		chassis.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
		chassis.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		chassis.setExpiration(0.1);
	}

    /**
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous()
	{
        chassis.setSafetyEnabled(false);
		
		SmartDashboard.putNumber("Shooter Power", frontShooterSpeed);
		setShooter(frontShooterSpeed, rearShooterSpeed);
		Timer.delay(5.0);
		
		TimedState tsFeed = null;
		for (int i = 1; i <= 3; i++)
		{
			do
			{
				if (tsFeed == null || tsFeed.check())
				{
					tsFeed = feedIn(tsFeed, false);
				}
			} while (tsFeed != null && isAutonomous());
			
			if (i == 1)
			{
				Timer.delay(2.5);
			}
			else if (i == 2)
			{
				Timer.delay(2.7);
			}
		}
		
		Timer.delay(1.0);
		setShooter(frontMinShooterSpeed, rearMinShooterSpeed);
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl()
	{
		TimedState tsFeed = null;
		TimedState tsRelease = null;
		
        chassis.setSafetyEnabled(true);
		back.setDirection(Relay.Direction.kReverse);
		
        while (isOperatorControl() && isEnabled())
        {
			if (tsFeed == null)
			{
				if (buttonFeed.isPressed())
				{
					tsFeed = feedIn(tsFeed, false);
				}
			}
			else if (tsFeed.check())
			{
				tsFeed = feedIn(tsFeed, false);
			}
			
			if (buttonRelease.isDown())
			{
				back.setDirection(Relay.Direction.kReverse);
			}
			else
			{
				back.set(Relay.Value.kOff);
			}
			
			if (tsRelease == null)
			{
				if (buttonRelease2.isPressed())
				{
					tsRelease = release(tsRelease);
				}
			}
			else if (tsRelease.check())
			{
				tsRelease = release(tsRelease);
			}
			
			if (buttonShoot.isDown())
			{
				if (stick1.getRawButton(7))
				{
					SmartDashboard.putNumber("Shooter Power", 1.0);
					setShooter(1.0, 1.0);
				}
				else
				{
					SmartDashboard.putNumber("Shooter Power", frontShooterSpeed);
					setShooter(frontShooterSpeed, rearShooterSpeed);
				}
			}
			else
			{
				setShooter(frontMinShooterSpeed, rearMinShooterSpeed);
			}
			
			adjustAxes();
			chassis.mecanumDrive_Cartesian(calX, calY, calTwist, calGyroAngle);

			winchDrive(calWinch);

			updateButtons();

			Timer.delay(0.005);			// wait for a motor update time
        }
    }
	
    /**
     * This function is called once each time the robot enters test mode.
     */
    public void test()
	{
    
    }

    private void adjustAxes()
    {
		calX = stick1.getY();
		calY = stick1.getX();
		calTwist = stick1.getTwist();
		calWinch = stick2.getY();

		calX = Math.abs(calX) < .15
			? 0
			: (1.04167 * calX * Math.abs(calX)) - .0416667;

		calY = Math.abs(calY) < .2
			? 0
			: (1.04167 * calY * Math.abs(calY)) - .0416667;

		//		twistfactor = stick->GetTwist();
		//		if (twistfactor <= 0) twistfactor = 2;
		//		else if (twistfactor > 0) twistfactor = 1;
		double twistfactor = (stick1.getThrottle() + 1) / 2 + 1;
		calTwist = Math.abs(calTwist) < .2
			? 0
			: -((1.04167 * calTwist * Math.abs(calTwist)) - .0416667) / twistfactor;

		SmartDashboard.putNumber("Twist Divisor", twistfactor);

		calWinch = Math.abs(calWinch) < .2
			? 0
			: ((1.04167 * calWinch * Math.abs(calWinch)) - .0416667);
    }

    
	TimedState feedIn(TimedState timedState, boolean isContinuous)
	{
		if (timedState == null)
		{
			feed.setAngle(180.0);
			return new TimedState(initTimer, 0.5, 1);
		}

		switch (timedState.get())
		{
		case 0:
			feed.setAngle(180.0);
			return new TimedState(initTimer, SmartDashboard.getNumber("Continuous Fire Delay"), 1);
			
		case 1:
			feed.setAngle(0);
			return isContinuous
				? new TimedState(initTimer, SmartDashboard.getNumber("Continuous Feed Delay"), 0)
				: new TimedState(initTimer, 0.5, 2);
			
		case 2:
			feed.setAngle(90);
			return null;
			
		default:
			return null;
		}
	}
	
	TimedState release(TimedState timedState)
	{
		if (timedState == null)
		{
			release.setAngle(180);
			return new TimedState(initTimer, 0.6, 1);
		}

		switch(timedState.get())
		{
		case 1:
			release.setAngle(0);
			return null;
			
		default:
			return null;
		}
	}

	void setShooter(double front, double back)
	{
		shootFront.set(front);
		shootRear.set(back);
	}
	
	void updateButtons()
	{
		// Put pushbuttons in here so their states get updated every loop
		buttonShoot.update();
		buttonRelease.update();
		buttonFeed.update();
		buttonRelease2.update();
	}
	void winchDrive(double val)
	{
		if (!winchUnwound.get() && !winchSpool.get())
		{
			winch.set(val);
		}
		else if (winchSpool.get() && val > 0)
		{
			winch.set(val);
		}
		else if (winchUnwound.get() && val < 0)
		{
			winch.set(val);
		}
		else
		{
			winch.set(0);
		}

		SmartDashboard.putBoolean(" Winch Wound In", winchSpool.get());
		SmartDashboard.putBoolean(" Winch Wound Out", winchUnwound.get());
		SmartDashboard.putBoolean(" Bar Grabbed", (winchBarLeft.get() && winchBarRight.get()));
		SmartDashboard.putBoolean(" R", winchBarRight.get());
		SmartDashboard.putBoolean(" L", winchBarLeft.get());
		SmartDashboard.putBoolean(" Flag Pole Left", flagPoleLeft.get());
		SmartDashboard.putBoolean(" Flag Pole Right", flagPoleRight.get());
		SmartDashboard.putNumber("Winch Power", val);
	}
}
