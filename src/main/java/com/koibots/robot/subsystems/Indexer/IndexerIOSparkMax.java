// Copyright (c) 2024 FRC 8230 - The KoiBots
// https://github.com/koibots8230

package com.koibots.robot.subsystems.Indexer;

import static edu.wpi.first.units.Units.*;

import com.koibots.robot.Constants.IndexerConstants;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

public class IndexerIOSparkMax implements IndexerIO {
    private final CANSparkMax motor;
    private final RelativeEncoder encoder;

    public IndexerIOSparkMax() {
        motor = new CANSparkMax(IndexerConstants.MOTOR, MotorType.kBrushless);
        motor.setIdleMode(IdleMode.kBrake);
        encoder = motor.getEncoder();
    }

    @Override
    public void updateInputs(IndexerIOInputs inputs) {
        inputs.velocity = RevolutionsPerSecond.of(encoder.getVelocity());
        if (motor.getIdleMode() == IdleMode.kBrake) {
            inputs.mode = true;
        } else {
            inputs.mode = false;
        }
    }

    public void runMotor(double speed) {
        motor.set(speed);
    }

    public void setIdle(boolean mode) {
        motor.setIdleMode(mode ? IdleMode.kBrake : IdleMode.kCoast);
    }
}
