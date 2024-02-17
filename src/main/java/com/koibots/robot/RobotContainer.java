// Copyright (c) 2024 FRC 8230 - The KoiBots
// https://github.com/koibots8230

package com.koibots.robot;

import static com.koibots.robot.subsystems.Subsystems.Intake;
import static com.koibots.robot.subsystems.Subsystems.Swerve;
import static edu.wpi.first.units.Units.Volts;

import com.koibots.lib.sysid.SysIDMechanism;
import com.koibots.robot.autos.AutonomousRegister;
import com.koibots.robot.autos.SysID;
import com.koibots.robot.commands.Swerve.FieldOrientedDrive;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

public class RobotContainer {
    GenericHID controller;

    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    public RobotContainer() {
        controller = new GenericHID(0);
    }

    public void registerAutos() {
        AutonomousRegister.registerAutoRoutine(
                "Sysid-Elevator",
                new SysID(SysIDMechanism.Elevator, () -> controller.getRawButton(1)));
        AutonomousRegister.registerAutoRoutine(
                "Sysid-Swerve", new SysID(SysIDMechanism.Swerve, () -> controller.getRawButton(1)));
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be created by
     * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling passing it to a
     * {@link JoystickButton}.
     */
    public void configureButtonBindings() {

        Swerve.get()
                .setDefaultCommand(
                        new FieldOrientedDrive(
                                () -> -controller.getRawAxis(1),
                                () -> -controller.getRawAxis(0),
                                () -> -controller.getRawAxis(4),
                                () -> controller.getPOV(),
                                () -> controller.getRawButton(1)));

        Intake.get()
                .setDefaultCommand(
                        new ConditionalCommand(
                                new InstantCommand(
                                        () -> Intake.get().setVoltage(Volts.of(12)), Intake.get()),
                                new InstantCommand(
                                        () -> Intake.get().setVoltage(Volts.of(0)), Intake.get()),
                                () -> controller.getRawButton(5)));

        // Elevator.get()
        //         .setDefaultCommand(
        //                 new InstantCommand(
        //                         () -> Elevator.get().setPostion(ElevatorConstants.LOAD_POSITION),
        //                         Elevator.get()));
        // PlopperPivot.get()
        //         .setDefaultCommand(
        //                 new InstantCommand(
        //                         () ->
        //                                 PlopperPivot.get()
        //
        // .setPosition(PlopperPivotConstants.LOAD_POSITION),
        //                         PlopperPivot.get()));
    }
}
