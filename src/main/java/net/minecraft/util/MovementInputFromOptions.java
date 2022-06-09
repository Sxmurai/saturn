package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        moveStrafe = 0.0F;
        moveForward = 0.0F;

        if (gameSettings.keyBindForward.isKeyDown())
        {
            ++moveForward;
        }

        if (gameSettings.keyBindBack.isKeyDown())
        {
            --moveForward;
        }

        if (gameSettings.keyBindLeft.isKeyDown())
        {
            ++moveStrafe;
        }

        if (gameSettings.keyBindRight.isKeyDown())
        {
            --moveStrafe;
        }

        jump = gameSettings.keyBindJump.isKeyDown();
        sneak = gameSettings.keyBindSneak.isKeyDown();

        if (sneak)
        {
            moveStrafe = (float)((double) moveStrafe * 0.3D);
            moveForward = (float)((double) moveForward * 0.3D);
        }
    }
}
