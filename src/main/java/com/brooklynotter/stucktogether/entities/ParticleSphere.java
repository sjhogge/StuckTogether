package com.brooklynotter.stucktogether.entities;

import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import static net.minecraft.util.Mth.floor;

public class ParticleSphere {

    public ServerLevel world;
    public BlockPos center;

    // https://github.com/EndaHealion/Fibonacci-Sphere/blob/master/src/Main.java
    private Vector3d[] generateFibSphere(int n, float radius) {
        Vector3d[] points = new Vector3d[n];

        final float phi = ((float)Math.sqrt(5)+1)/2 - 1;

        for (int i = 0; i < points.length; i++) {
            float longitude = phi * (float)(Math.PI) * 2 * i;
            longitude /= (2 * Math.PI);
            longitude -= floor(longitude);
            longitude *= (float)Math.PI * 2;

            if (longitude > Math.PI) {
                longitude -= (2 * Math.PI);
            }

            final float latitude = (float)Math.asin(-1 + 2 * i/(float)n);
            final float cosOfLatitude = (float)Math.cos(latitude);
            points[i] = new Vector3d(
                    radius * cosOfLatitude * (float)Math.cos(longitude),
                    radius * cosOfLatitude * (float)Math.sin(longitude),
                    radius * (float)Math.sin(latitude)
            );
        }
        return points;
    }

    public void SpawnSphereParticles(float sphereRadius, ParticleOptions particleOptions){
        Vector3d[] spherePoints = this.generateFibSphere((int)sphereRadius * 100, sphereRadius);

        for (Vector3d spherePoint : spherePoints) {
            double x = spherePoint.x + center.getX();
            double y = spherePoint.y + center.getY();
            double z = spherePoint.z + center.getZ();
            world.sendParticles(particleOptions, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}
