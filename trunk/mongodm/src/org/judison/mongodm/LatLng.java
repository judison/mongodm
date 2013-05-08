/*
 * Copyright (c) 2013, Judison Oliveira Gil Filho <judison@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.judison.mongodm;

import java.util.Iterator;
import java.util.Set;

public class LatLng {

	private static final double EARTH_MEAN_RADIUS = 6371009; // in Meters

	private final double lat;
	private final double lng;

	public LatLng(double lat, double lng) {
		this.lat = normalizeLatitude(lat);
		this.lng = normalizeLongitude(lng);
		if (Double.isNaN(this.lat) || Double.isNaN(this.lng))
			throw new IllegalArgumentException();
	}

	public LatLng(MObject obj) {
		if (obj == null)
			throw new IllegalArgumentException();

		if (obj instanceof MList) {
			this.lng = normalizeLongitude(((Number)((MList)obj).get(0)).doubleValue());
			this.lat = normalizeLatitude(((Number)((MList)obj).get(1)).doubleValue());
		} else if (obj.contains("lng") && obj.contains("lat")) {
			this.lng = normalizeLongitude(((Number)obj.get("lng")).doubleValue());
			this.lat = normalizeLatitude(((Number)obj.get("lat")).doubleValue());
		} else if (obj.contains("lon") && obj.contains("lat")) {
			this.lng = normalizeLongitude(((Number)obj.get("lon")).doubleValue());
			this.lat = normalizeLatitude(((Number)obj.get("lat")).doubleValue());
		} else if (obj.contains("longitude") && obj.contains("latitude")) {
			this.lng = normalizeLongitude(((Number)obj.get("longitude")).doubleValue());
			this.lat = normalizeLatitude(((Number)obj.get("latitude")).doubleValue());
		} else {
			Set<String> flds = obj.keySet();
			if (flds.size() == 2) {
				Iterator<String> iter = flds.iterator();
				this.lng = normalizeLongitude(((Number)obj.get(iter.next())).doubleValue());
				this.lat = normalizeLatitude(((Number)obj.get(iter.next())).doubleValue());
			}
			throw new IllegalArgumentException("Not a LatLng object");
		}
		if (Double.isNaN(this.lat) || Double.isNaN(this.lng))
			throw new IllegalArgumentException();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof LatLng))
			return false;

		LatLng other = (LatLng)obj;
		return this.lat == other.lat && this.lng == this.lng;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lng);
		result = prime * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "{ lat: " + lat + ", lng:" + lng + " }";
	}

	public MObject toMObject() {
		MObject obj = new MObject();
		obj.set("lng", lng);
		obj.set("lat", lat);
		return obj;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lng;
	}

	/**
	 * @param other the other point
	 * @return the distance between this point and the other point in meters
	 */
	public double getDistanceTo(LatLng other) {
		return distanceInRadians(this.lat, this.lng, other.lat, other.lng) * EARTH_MEAN_RADIUS;
	}

	/**
	 * @param lat the other point's latitude
	 * @param lng the other point's longitude
	 * @return the distance between this point and the other point in meters
	 */
	public double getDistanceTo(double lat, double lng) {
		return distanceInRadians(this.lat, this.lng, normalizeLatitude(lat), normalizeLongitude(lng)) * EARTH_MEAN_RADIUS;
	}

	/**
	 * @param other the other point
	 * @return the initial bearing to the other point, in degrees
	 */
	public double bearingTo(LatLng other) {
		return normalizeBearing(Math.toDegrees(initialBearingInRadians(this.lat, this.lng, other.lat, other.lng)));
	}

	/**
	 * @param lat the other point's latitude
	 * @param lng the other point's longitude
	 * @return the initial bearing to the other point, in degrees
	 */
	public double bearingTo(double lat, double lng) {
		return normalizeBearing(Math.toDegrees(initialBearingInRadians(this.lat, this.lng, normalizeLatitude(lat), normalizeLongitude(lng))));
	}

	private static final double normalizeLongitude(final double lng) {
		if (Double.isNaN(lng) || Double.isInfinite(lng))
			return Double.NaN;
		double longitudeResult = lng % 360;
		if (longitudeResult > 180) {
			double diff = longitudeResult - 180;
			longitudeResult = -180 + diff;
		} else if (longitudeResult < -180) {
			double diff = longitudeResult + 180;
			longitudeResult = 180 + diff;
		}
		return longitudeResult;
	}

	private static final double normalizeLatitude(final double lat) {
		if (Double.isNaN(lat) || Double.isInfinite(lat))
			return Double.NaN;
		if (lat > 0) {
			return Math.min(lat, 90.0);
		} else {
			return Math.max(lat, -90.0);
		}
	}

	private static final double normalizeBearing(final double bearing) {
		if (Double.isNaN(bearing) || Double.isInfinite(bearing))
			return Double.NaN;
		double bearingResult = bearing % 360;
		if (bearingResult < 0)
			bearingResult += 360;
		return bearingResult;
	}

	private static final double distanceInRadians(final double lat1, final double lng1, final double lat2, final double lng2) {
		double lat1R = Math.toRadians(lat1);
		double lat2R = Math.toRadians(lat2);
		double dLatR = Math.abs(lat2R - lat1R);
		double dLngR = Math.abs(Math.toRadians(lng2 - lng1));
		double a = Math.sin(dLatR / 2) * Math.sin(dLatR / 2) + Math.cos(lat1R) * Math.cos(lat2R) * Math.sin(dLngR / 2) * Math.sin(dLngR / 2);
		return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	private static final double initialBearingInRadians(final double lat1, final double lng1, final double lat2, final double lng2) {
		double lat1R = Math.toRadians(lat1);
		double lat2R = Math.toRadians(lat2);
		double dLngR = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLngR) * Math.cos(lat2R);
		double b = Math.cos(lat1R) * Math.sin(lat2R) - Math.sin(lat1R) * Math.cos(lat2R) * Math.cos(dLngR);
		return Math.atan2(a, b);
	}

}
