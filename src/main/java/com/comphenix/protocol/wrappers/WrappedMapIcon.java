/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2021 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.EnumConverter;

/**
 * Represents a nms MapIcon as part of the Map Data packet.
 * @author dmulloy2
 */
public class WrappedMapIcon {
	public static Class<?> NMS_CLASS = MinecraftReflection.getMinecraftClass("MapIcon");

	private static Class<?> TYPE_CLASS = MinecraftReflection.getMinecraftClass("MapIcon$Type");
	private static EnumConverter<MapIconType> TYPE_CONVERTER = new EnumConverter<>(TYPE_CLASS, MapIconType.class);

	public enum MapIconType {
		PLAYER,
		FRAME,
		RED_MARKER,
		BLUE_MARKER,
		TARGET_X,
		TARGET_POINT,
		PLAYER_OFF_MAP,
		PLAYER_OFF_LIMITS,
		MANSION,
		MONUMENT,
		BANNER_WHITE,
		BANNER_ORANGE,
		BANNER_MAGENTA,
		BANNER_LIGHT_BLUE,
		BANNER_YELLOW,
		BANNER_LIME,
		BANNER_PINK,
		BANNER_GRAY,
		BANNER_LIGHT_GRAY,
		BANNER_CYAN,
		BANNER_PURPLE,
		BANNER_BLUE,
		BANNER_BROWN,
		BANNER_GREEN,
		BANNER_RED,
		BANNER_BLACK,
		RED_X
	}

	private MapIconType type;
	private byte x;
	private byte y;
	private byte rotation;
	private WrappedChatComponent name;

	/**
	 * Constructs a Map Icon with default values
	 */
	public WrappedMapIcon() {
		this.type = MapIconType.PLAYER;
		this.x = 0;
		this.y = 0;
		this.rotation = 0;
		this.name = null;
	}

	/**
	 * Constructs a new Map Icon
	 *
	 * @param type Icon type
	 * @param x X position [-128,127]
	 * @param y Y position [-128,127]
	 * @param rotation Rotation, in degrees [0,360]
	 * @param name Display name, may be null
	 */
	public WrappedMapIcon(@Nonnull MapIconType type, byte x, byte y, double rotation,
			@Nullable WrappedChatComponent name) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.setRotation(rotation);
		this.name = name;
	}

	/**
	 * Gets the type of this icon. Icon types change the display
	 * @return Map Icon Type
	 */
	public MapIconType getType() {
		return type;
	}

	/**
	 * @param type New MapIconType
	 */
	public void setType(MapIconType type) {
		this.type = type;
	}

	/**
	 * Gets the X coordinate of this icon on the map.
	 * -128 for furthest left, 127 for furthest right
	 *
	 * @return The X coordinate
	 */
	public byte getX() {
		return x;
	}

	/**
	 * @param x New X value [-128,127]
	 */
	public void setX(byte x) {
		this.x = x;
	}

	/**
	 * Gets the Y coordinate of this icon on the map.
	 * -128 for furthest left, 127 for furthest right
	 *
	 * @return The Y coordinate
	 */
	public byte getY() {
		return y;
	}

	/**
	 * @param y New Y value [-128,127]
	 */
	public void setY(byte y) {
		this.y = y;
	}

	/**
	 * Gets the rotation of this icon on the map from 0-360
	 * Note that internally the rotation is a byte from 0-15 and is scaled by 22.5 deg.
	 * @return The rotation
	 */
	public double getRotation() {
		return rotation * 22.5;
	}

	/**
	 * Set the new rotation. Rotation may be from 0-360 degrees
	 * @param rot New rotation value in degrees.
	 */
	public void setRotation(double rot) {
		this.rotation = (byte) (rot / 22.5);
	}

	/**
	 * Gets the display name of this icon. Note that it may be null.
	 * @return Display name
	 */
	@Nullable
	public WrappedChatComponent getName() {
		return name;
	}

	/**
	 * @param name New display name. May be null, in which case no name is displayed.
	 */
	public void setName(@Nullable WrappedChatComponent name) {
		this.name = name;
	}

	private static Constructor<?> constructor = null;

	/**
	 * Converts nms Map Icons to and from this ProtocolLib representation.
	 * @return The converter
	 */
	public static EquivalentConverter<WrappedMapIcon> getConverter() {
		return new EquivalentConverter<WrappedMapIcon>() {
			@Override
			public Object getGeneric(WrappedMapIcon specific) {
				if (constructor == null) {
					constructor = FuzzyReflection.fromClass(NMS_CLASS)
							.getConstructor(FuzzyMethodContract.newBuilder()
									.requirePublic()
									.returnTypeExact(NMS_CLASS)
									.parameterCount(5)
									.build()
							);
				}

				try {
					return constructor.newInstance(
							TYPE_CONVERTER.getGeneric(specific.type),
							specific.x,
							specific.y,
							specific.rotation,
							specific.name != null ? specific.name.getHandle() : null
					);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Failed to construct nms Map Icon", ex);
				}
			}

			@Override
			public WrappedMapIcon getSpecific(Object generic) {
				StructureModifier<Object> modifier = new StructureModifier<>(generic.getClass());
				StructureModifier<Byte> bytes = modifier.withType(byte.class);
				Object nmsComponent = modifier.withType(MinecraftReflection.getIChatBaseComponentClass()).read(0);

				return new WrappedMapIcon(
						modifier.withType(TYPE_CLASS, TYPE_CONVERTER).read(0),
						bytes.read(0),
						bytes.read(1),
						bytes.read(2) * 22.5,
						nmsComponent != null ? WrappedChatComponent.fromHandle(nmsComponent) : null
				);
			}

			@Override
			public Class<WrappedMapIcon> getSpecificType() {
				return WrappedMapIcon.class;
			}
		};
	}
}
