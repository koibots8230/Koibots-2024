// Copyright (c) 2024 FRC 8230 - The KoiBots
// https://github.com/koibots8230

package com.koibots.robot.commands.Swerve;

import static com.koibots.robot.subsystems.Subsystems.Swerve;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.RadiansPerSecond;

import com.koibots.robot.Constants.ControlConstants;
import com.koibots.robot.Constants.RobotConstants;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.Logger;

public class TestDrive extends Command {
    DoubleSupplier vxSupplier;
    DoubleSupplier vySupplier;
    DoubleSupplier vThetaSupplier;
    DoubleSupplier angleSupplier;
    BooleanSupplier crossSupplier;

    List<SendableChooser<Boolean>> modules;

    ProfiledPIDController angleAlignmentController;

    SlewRateLimiter vxLimiter;
    SlewRateLimiter vyLimiter;
    SlewRateLimiter vThetaLimiter;

    public TestDrive(
            DoubleSupplier vxSupplier,
            DoubleSupplier vySupplier,
            DoubleSupplier vThetaSupplier,
            DoubleSupplier angleSupplier,
            BooleanSupplier crossSupplier,
            List<SendableChooser<Boolean>> modules) {
        this.vxSupplier = vxSupplier;
        this.vySupplier = vySupplier;
        this.vThetaSupplier = vThetaSupplier;
        this.angleSupplier = angleSupplier;
        this.crossSupplier = crossSupplier;
        this.modules = modules;

        angleAlignmentController =
                new ProfiledPIDController(
                        0.19,
                        0,
                        0,
                        new Constraints(
                                RobotConstants.MAX_ANGULAR_VELOCITY.in(RadiansPerSecond),
                                4 * Math.PI),
                        0.02);

        angleAlignmentController.enableContinuousInput(0, 2 * Math.PI);

        SmartDashboard.putData("Angle Alignment Controller", angleAlignmentController);

        // vxLimiter = new SlewRateLimiter(0.3);
        // vyLimiter = new SlewRateLimiter(0.3);
        // vThetaLimiter = new SlewRateLimiter(0.3);

        addRequirements(Swerve.get());
    }

    @Override
    public void execute() {

        if (!this.crossSupplier.getAsBoolean()) { // Normal Field Oriented Drive
            // double vxInput = vxLimiter.calculate(vxSupplier.getAsDouble());
            // double vyInput = vyLimiter.calculate(vySupplier.getAsDouble());
            double vxInput = -vxSupplier.getAsDouble();
            double vyInput = -vySupplier.getAsDouble();

            double linearMagnitude =
                    MathUtil.applyDeadband(
                            Math.hypot(vxInput, vyInput), ControlConstants.DEADBAND, 1);

            Rotation2d linearDirection = new Rotation2d(vxInput, vyInput);

            double angularVelocity;

            if (angleSupplier.getAsDouble() != -1) {
                angularVelocity =
                        angleAlignmentController.calculate(
                                Swerve.get().getEstimatedPose().getRotation().getRadians(),
                                Math.toRadians(angleSupplier.getAsDouble()) - Math.PI);

                Logger.recordOutput(
                        "Angle Alignment Setpoint",
                        Math.toRadians(angleSupplier.getAsDouble()) - Math.PI);
                Logger.recordOutput("Angle Alignement Output", angularVelocity);
            } else {
                angularVelocity =
                        // MathUtil.applyDeadband(vThetaLimiter.calculate(vThetaSupplier.getAsDouble()), ControlConstants.DEADBAND);
                        MathUtil.applyDeadband(
                                vThetaSupplier.getAsDouble(), ControlConstants.DEADBAND);
            }

            // Apply Scaling
            linearMagnitude *= linearMagnitude * Math.signum(linearMagnitude);
            angularVelocity *= angularVelocity * angularVelocity;

            ChassisSpeeds speeds =
                    ChassisSpeeds.fromFieldRelativeSpeeds(
                            linearMagnitude
                                    * linearDirection.getCos()
                                    * RobotConstants.MAX_LINEAR_SPEED.in(MetersPerSecond),
                            linearMagnitude
                                    * linearDirection.getSin()
                                    * RobotConstants.MAX_LINEAR_SPEED.in(MetersPerSecond),
                            angularVelocity
                                    * RobotConstants.MAX_ANGULAR_VELOCITY.in(RadiansPerSecond),
                            Swerve.get().getEstimatedPose().getRotation());

            boolean[] thing = {true, true, true, true};
            for (int a = 0; a < 4; a++) {
                thing[a] = modules.get(a).getSelected();
            }

            Swerve.get().driveRobotRelativeByModule(speeds, thing);
        } else { // Set Cross
            Swerve.get().setCross();
        }
    }
}
