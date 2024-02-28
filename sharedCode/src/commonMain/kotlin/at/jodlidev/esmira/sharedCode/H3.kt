package at.jodlidev.esmira.sharedCode

import kotlin.math.*

/*

 Created by SelinaDev, 26.2.2024

 This file contains a pure Kotlin implementation of the code necessary to calculate H3 geospatial indices from Locations (latitude, longitude).
 See https://h3geo.org/ for information on the H3 geospatial indexing system.
 See https://github.com/uber/h3 for the GitHub repository containing the C reference implementation this code is based on.

 */

// Constants

internal const val EPSILON = 0.0000000000000001
internal const val M_AP7_ROT_RADS = 0.3334731722518321
internal const val M_SQRT3_2 = 0.8660254037844386
internal const val M_SQRT7 = 2.6457513110645907
internal const val M_SIN60 = M_SQRT3_2
internal const val M_2PI = 6.283185307179586
internal const val M_PI_180 = 0.017453292519943295
internal const val NUM_ICOSA_FACES = 20
internal const val NUM_BASE_CELLS = 122
internal const val RES0_U_GNOMONIC = 0.381966011250105
internal const val MAX_H3_RES = 15
internal const val MAX_FACE_COORD = 2

internal const val H3_CELL_MODE = 1

internal typealias Direction = Int
internal const val CENTER_DIGIT = 0
internal const val K_AXES_DIGIT = 1
internal const val J_AXES_DIGIT = 2
internal const val JK_AXES_DIGIT = J_AXES_DIGIT or K_AXES_DIGIT
internal const val I_AXES_DIGIT = 4
internal const val IK_AXES_DIGIT = I_AXES_DIGIT or K_AXES_DIGIT
internal const val IJ_AXES_DIGIT = I_AXES_DIGIT or J_AXES_DIGIT
internal const val INVALID_DIGIT = 7
internal const val NUM_DIGITS = 7
internal const val PENTAGON_SKIPPED_DIGIT = K_AXES_DIGIT

internal val SQRT7_POWERS = arrayOf(
    1.0,
    2.6457513110645907,
    7.0,
    18.520259177452136,
    49.00000000000001,
    129.64181424216497,
    343.0000000000001,
    907.4926996951549,
    2401.000000000001,
    6352.448897866085,
    16807.000000000007,
    44467.1422850626,
    117649.00000000007,
    311269.9959954382,
    823543.0000000006,
    2178889.971968068,
    5764801.0,
)


class LatLng (var lat: Double, var lng: Double) {

    fun latLngToCell(res: Int): Result<H3Index> {
        if(res < 0 || res > MAX_H3_RES) {
            return Result.failure(Exception("Invalid resolution"))
        }
        if(!lat.isFinite() || !lng.isFinite()) {
            return Result.failure(Exception("Non-finite latitude or longitude"))
        }

        val fijk = geoToFaceIjk(res)
        val out = fijk.faceIjkToH3(res)
        if(out == H3.H3_NULL) {
            return Result.failure(Exception("Failed"))
        } else {
            return Result.success(out)
        }
    }

    fun setGeoDegs(latDegs: Double, lngDegs: Double) {
        lat = latDegs * M_PI_180
        lng = lngDegs * M_PI_180
    }
    internal fun geoAzimuthRads(other: LatLng): Double {
        return atan2(
            cos(other.lat) * sin(other.lng - this.lng),
            cos(this.lat) * sin(other.lat) - sin(this.lat) * cos(other.lat) * cos(other.lng - this.lng)
        )
    }

    internal fun geoToVec3d(): Vec3d {
        val v = Vec3d()
        val r = cos(lat)

        v.z = sin(lat)
        v.x = cos(lng) * r
        v.y = sin(lng) * r

        return v
    }

    internal fun geoToClosestFace(): Pair<Int, Double> {
        val v3d = geoToVec3d()

        var face = 0
        var sqd = 5.0
        for(f in 0..(NUM_ICOSA_FACES-1)) {
            val sqdT = FaceIJK.faceCenterPoint[f].pointSquareDist(v3d)
            if (sqdT < sqd) {
                face = f
                sqd = sqdT
            }
        }

        return Pair(face, sqd)
    }

    internal fun geoToHex2d(res: Int): Pair<Int, Vec2d> {
        val v = Vec2d()
        val (face, sqd) = geoToClosestFace()
        var r = acos(1.0 - sqd / 2.0)

        if (r < EPSILON) {
            v.x = 0.0
            v.y = 0.0
            return Pair(face, v)
        }

        var theta = posAngleRads(FaceIJK.faceAxesAzRadsCII[face][0] - posAngleRads(FaceIJK.faceCenterGeo[face].geoAzimuthRads(this)))
        if (H3.isResolutionClassIII(res)) theta = posAngleRads(theta - M_AP7_ROT_RADS)

        r = tan(r)
        r /= RES0_U_GNOMONIC
        for(i in 1..res) r *= M_SQRT7

        v.x = r * cos(theta)
        v.y = r * sin(theta)

        return Pair(face, v)
    }

    internal fun geoToFaceIjk(res: Int): FaceIJK {
        var h = FaceIJK()
        val (face, v) = geoToHex2d(res)
        h.face = face
        h.coord = v.hex2dToCoordIJK()
        return h
    }

    companion object {
        internal fun posAngleRads(rads: Double): Double {
            var tmp = if(rads < 0.0) rads + M_2PI else rads
            if(rads >= M_2PI) tmp -= M_2PI
            return tmp
        }

        fun fromDegrees(latDegs: Double, lngDegs: Double): LatLng {
            val latLng = LatLng(0.0, 0.0)
            latLng.setGeoDegs(latDegs, lngDegs)
            return latLng
        }
    }
}

class Vec2d (var x: Double = 0.0, var y: Double = 0.0) {
    internal fun hex2dToCoordIJK(): CoordIJK {
        val h = CoordIJK()
        h.k = 0

        val a1 = abs(x)
        val a2 = abs(y)

        val x2 = a2 / M_SIN60
        val x1 = a1 + x2 / 2.0

        val m1 = x1.toInt()
        val m2 = x2.toInt()

        val r1 = x1 - m1.toDouble()
        val r2 = x2 - m2.toDouble()

        if (r1 < 0.5) {
            if (r1 < 1.0 / 3.0) {
                if (r2 < (1.0 + r1) / 2.0) {
                    h.i = m1
                    h.j = m2
                } else {
                    h.i = m1
                    h.j = m2 + 1
                }
            } else {
                if (r2 < (1.0 - r1)) {
                    h.j = m2
                } else {
                    h.j = m2 + 1
                }

                if ((1.0 - r1) <= r2 && r2 < (2.0 * r1)) {
                    h.i = m1 + 1
                } else {
                    h.i = m1
                }
            }
        } else {
            if (r1 < 2.0 / 3.0) {
                if (r2 < (1.0 - r1)) {
                    h.j = m2
                } else {
                    h.j = m2 + 1
                }

                if ((2.0 * r1 - 1.0) < r2 && r2 < (1.0 - r1)) {
                    h.i = m1
                } else {
                    h.i = m1 + 1
                }
            } else {
                if (r2 < (r1 / 2.0)) {
                    h.i = m1 + 1
                    h.j = m2
                } else {
                    h.i = m1 + 1
                    h.j = m2 + 1
                }
            }
        }

        if(x < 0.0) {
            if((h.j % 2) == 0) {
                val axisi = h.j.toLong() / 2L
                val diff = h.i.toLong() - axisi
                h.i -= (2.0 * diff.toDouble()).toInt()
            } else {
                val axisi = (h.j + 1).toLong() / 2L
                val diff = h.i.toLong() - axisi
                h.i -= (2.0 * diff.toDouble()).toInt() + 1
            }
        }

        if(y < 0.0) {
            h.i -= (2 * h.j + 1) / 2
            h.j *= -1
        }

        h.ijkNormalize()

        return h
    }
}

class Vec3d (var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    internal fun pointSquareDist(other: Vec3d): Double {
        return square(this.x - other.x) + square(this.y - other.y) + square(this.z - other.z)
    }

    companion object {
        internal fun square(x: Double): Double { return x * x}
    }
}

class CoordIJK (var i: Int = 0, var j: Int = 0, var k: Int = 0) {
    internal fun ijkNormalize() {
        if(i < 0) {
            j -= i
            k -= i
            i = 0
        }
        if(j < 0) {
            i -= j
            k -= j
            j = 0
        }
        if(k < 0) {
            i -= k
            j -= k
            k = 0
        }
        val minimum = min(i, min(j, k))
        if(minimum > 0) {
            i -= minimum
            j -= minimum
            k -= minimum
        }
    }

    internal fun ijkMatches(other: CoordIJK): Boolean {
        return this.i == other.i && this.j == other.j && this.k == other.k
    }

    internal fun ijkAdd(other: CoordIJK): CoordIJK {
        return CoordIJK(
            this.i + other.i,
            this.j + other.j,
            this.k + other.k
        )
    }

    internal fun ijkSub(other: CoordIJK): CoordIJK {
        return CoordIJK(
            this.i - other.i,
            this.j - other.j,
            this.k - other.k
        )
    }

    internal fun ijkScale(factor: Int) {
        i *= factor
        j *= factor
        k *= factor
    }

    internal fun set(value: CoordIJK) {
        i = value.i
        j = value.j
        k = value.k
    }
    internal fun unitIjkToDigit(): Direction {
        val c = CoordIJK(i, j, k)
        c.ijkNormalize()

        var digit = INVALID_DIGIT
        for(i in CENTER_DIGIT..(NUM_DIGITS-1)) {
            if(c.ijkMatches(UNIT_VECS[i])){
                digit = i
                break
            }
        }
        return digit
    }

    internal fun upAp7() {
        val iCalc = i - k
        val jCalc = j - k

        i = round((3 * iCalc - jCalc) / 7.0).toInt()
        j = round((iCalc + 2 * jCalc) / 7.0).toInt()
        k = 0
        ijkNormalize()
    }

    internal fun upAp7r() {
        val iCalc = i - k
        val jCalc = j - k

        i = round((2 * iCalc + jCalc) / 7.0).toInt()
        j = round((3 * jCalc - iCalc) / 7.0).toInt()
        k = 0
        ijkNormalize()
    }

    internal fun downAp7() {
        val iVec = CoordIJK(3, 0, 1)
        val jVec = CoordIJK(1, 3, 0)
        val kVec = CoordIJK(0, 1, 3)

        iVec.ijkScale(i)
        jVec.ijkScale(j)
        kVec.ijkScale(k)

        set(iVec.ijkAdd(jVec).ijkAdd(kVec))

        ijkNormalize()
    }

    internal fun downAp7r() {
        val iVec = CoordIJK(3, 1, 0)
        val jVec = CoordIJK(0, 3, 1)
        val kVec = CoordIJK(1, 0, 3)

        iVec.ijkScale(i)
        jVec.ijkScale(j)
        kVec.ijkScale(k)

        set(iVec.ijkAdd(jVec).ijkAdd(kVec))

        ijkNormalize()
    }

    companion object {
        val UNIT_VECS = arrayOf(
            CoordIJK(0, 0, 0),
            CoordIJK(0, 0, 1),
            CoordIJK(0, 1, 0),
            CoordIJK(0, 1, 1),
            CoordIJK(1, 0, 0),
            CoordIJK(1, 0, 1),
            CoordIJK(1, 1, 0)
        )
    }
}

data class BaseCellRotation (val baseCell: Int, val ccwRot60: Int)
data class BaseCellData (val homeFijk: FaceIJK, val isPentagon: Boolean, val cwOffsetPent: IntArray)
class BaseCells {
    companion object {
        internal val faceIjkBaseCells = arrayOf(
            arrayOf(// face 0
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(16, 0), BaseCellRotation(18, 0), BaseCellRotation(24, 0)),  // j 0
                    arrayOf(BaseCellRotation(33, 0), BaseCellRotation(30, 0), BaseCellRotation(32, 3)),  // j 1
                    arrayOf(BaseCellRotation(49, 1), BaseCellRotation(48, 3), BaseCellRotation(50, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(8, 0), BaseCellRotation(5, 5), BaseCellRotation(10, 5)),    // j 0
                    arrayOf(BaseCellRotation(22, 0), BaseCellRotation(16, 0), BaseCellRotation(18, 0)),  // j 1
                    arrayOf(BaseCellRotation(41, 1), BaseCellRotation(33, 0), BaseCellRotation(30, 0))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(4, 0), BaseCellRotation(0, 5), BaseCellRotation(2, 5)),    // j 0
                    arrayOf(BaseCellRotation(15, 1), BaseCellRotation(8, 0), BaseCellRotation(5, 5)),   // j 1
                    arrayOf(BaseCellRotation(31, 1), BaseCellRotation(22, 0), BaseCellRotation(16, 0))  // j 2
                )),
            arrayOf(// face 1
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(2, 0), BaseCellRotation(6, 0), BaseCellRotation(14, 0)),    // j 0
                    arrayOf(BaseCellRotation(10, 0), BaseCellRotation(11, 0), BaseCellRotation(17, 3)),  // j 1
                    arrayOf(BaseCellRotation(24, 1), BaseCellRotation(23, 3), BaseCellRotation(25, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(0, 0), BaseCellRotation(1, 5), BaseCellRotation(9, 5)),    // j 0
                    arrayOf(BaseCellRotation(5, 0), BaseCellRotation(2, 0), BaseCellRotation(6, 0)),    // j 1
                    arrayOf(BaseCellRotation(18, 1), BaseCellRotation(10, 0), BaseCellRotation(11, 0))  // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(4, 1), BaseCellRotation(3, 5), BaseCellRotation(7, 5)),  // j 0
                    arrayOf(BaseCellRotation(8, 1), BaseCellRotation(0, 0), BaseCellRotation(1, 5)),  // j 1
                    arrayOf(BaseCellRotation(16, 1), BaseCellRotation(5, 0), BaseCellRotation(2, 0))  // j 2
                )),
            arrayOf(// face 2
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(7, 0), BaseCellRotation(21, 0), BaseCellRotation(38, 0)),  // j 0
                    arrayOf(BaseCellRotation(9, 0), BaseCellRotation(19, 0), BaseCellRotation(34, 3)),  // j 1
                    arrayOf(BaseCellRotation(14, 1), BaseCellRotation(20, 3), BaseCellRotation(36, 3))  // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(3, 0), BaseCellRotation(13, 5), BaseCellRotation(29, 5)),  // j 0
                    arrayOf(BaseCellRotation(1, 0), BaseCellRotation(7, 0), BaseCellRotation(21, 0)),   // j 1
                    arrayOf(BaseCellRotation(6, 1), BaseCellRotation(9, 0), BaseCellRotation(19, 0))    // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(4, 2), BaseCellRotation(12, 5), BaseCellRotation(26, 5)),  // j 0
                    arrayOf(BaseCellRotation(0, 1), BaseCellRotation(3, 0), BaseCellRotation(13, 5)),   // j 1
                    arrayOf(BaseCellRotation(2, 1), BaseCellRotation(1, 0), BaseCellRotation(7, 0))     // j 2
                )),
            arrayOf(// face 3
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(26, 0), BaseCellRotation(42, 0), BaseCellRotation(58, 0)),  // j 0
                    arrayOf(BaseCellRotation(29, 0), BaseCellRotation(43, 0), BaseCellRotation(62, 3)),  // j 1
                    arrayOf(BaseCellRotation(38, 1), BaseCellRotation(47, 3), BaseCellRotation(64, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(12, 0), BaseCellRotation(28, 5), BaseCellRotation(44, 5)),  // j 0
                    arrayOf(BaseCellRotation(13, 0), BaseCellRotation(26, 0), BaseCellRotation(42, 0)),  // j 1
                    arrayOf(BaseCellRotation(21, 1), BaseCellRotation(29, 0), BaseCellRotation(43, 0))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(4, 3), BaseCellRotation(15, 5), BaseCellRotation(31, 5)),  // j 0
                    arrayOf(BaseCellRotation(3, 1), BaseCellRotation(12, 0), BaseCellRotation(28, 5)),  // j 1
                    arrayOf(BaseCellRotation(7, 1), BaseCellRotation(13, 0), BaseCellRotation(26, 0))   // j 2
                )),
            arrayOf(// face 4
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(31, 0), BaseCellRotation(41, 0), BaseCellRotation(49, 0)),  // j 0
                    arrayOf(BaseCellRotation(44, 0), BaseCellRotation(53, 0), BaseCellRotation(61, 3)),  // j 1
                    arrayOf(BaseCellRotation(58, 1), BaseCellRotation(65, 3), BaseCellRotation(75, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(15, 0), BaseCellRotation(22, 5), BaseCellRotation(33, 5)),  // j 0
                    arrayOf(BaseCellRotation(28, 0), BaseCellRotation(31, 0), BaseCellRotation(41, 0)),  // j 1
                    arrayOf(BaseCellRotation(42, 1), BaseCellRotation(44, 0), BaseCellRotation(53, 0))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(4, 4), BaseCellRotation(8, 5), BaseCellRotation(16, 5)),    // j 0
                    arrayOf(BaseCellRotation(12, 1), BaseCellRotation(15, 0), BaseCellRotation(22, 5)),  // j 1
                    arrayOf(BaseCellRotation(26, 1), BaseCellRotation(28, 0), BaseCellRotation(31, 0))   // j 2
                )),
            arrayOf(// face 5
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(50, 0), BaseCellRotation(48, 0), BaseCellRotation(49, 3)),  // j 0
                    arrayOf(BaseCellRotation(32, 0), BaseCellRotation(30, 3), BaseCellRotation(33, 3)),  // j 1
                    arrayOf(BaseCellRotation(24, 3), BaseCellRotation(18, 3), BaseCellRotation(16, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(70, 0), BaseCellRotation(67, 0), BaseCellRotation(66, 3)),  // j 0
                    arrayOf(BaseCellRotation(52, 3), BaseCellRotation(50, 0), BaseCellRotation(48, 0)),  // j 1
                    arrayOf(BaseCellRotation(37, 3), BaseCellRotation(32, 0), BaseCellRotation(30, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(83, 0), BaseCellRotation(87, 3), BaseCellRotation(85, 3)),  // j 0
                    arrayOf(BaseCellRotation(74, 3), BaseCellRotation(70, 0), BaseCellRotation(67, 0)),  // j 1
                    arrayOf(BaseCellRotation(57, 1), BaseCellRotation(52, 3), BaseCellRotation(50, 0))   // j 2
                )),
            arrayOf(// face 6
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(25, 0), BaseCellRotation(23, 0), BaseCellRotation(24, 3)),  // j 0
                    arrayOf(BaseCellRotation(17, 0), BaseCellRotation(11, 3), BaseCellRotation(10, 3)),  // j 1
                    arrayOf(BaseCellRotation(14, 3), BaseCellRotation(6, 3), BaseCellRotation(2, 3))     // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(45, 0), BaseCellRotation(39, 0), BaseCellRotation(37, 3)),  // j 0
                    arrayOf(BaseCellRotation(35, 3), BaseCellRotation(25, 0), BaseCellRotation(23, 0)),  // j 1
                    arrayOf(BaseCellRotation(27, 3), BaseCellRotation(17, 0), BaseCellRotation(11, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(63, 0), BaseCellRotation(59, 3), BaseCellRotation(57, 3)),  // j 0
                    arrayOf(BaseCellRotation(56, 3), BaseCellRotation(45, 0), BaseCellRotation(39, 0)),  // j 1
                    arrayOf(BaseCellRotation(46, 3), BaseCellRotation(35, 3), BaseCellRotation(25, 0))   // j 2
                )),
            arrayOf(// face 7
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(36, 0), BaseCellRotation(20, 0), BaseCellRotation(14, 3)),  // j 0
                    arrayOf(BaseCellRotation(34, 0), BaseCellRotation(19, 3), BaseCellRotation(9, 3)),   // j 1
                    arrayOf(BaseCellRotation(38, 3), BaseCellRotation(21, 3), BaseCellRotation(7, 3))    // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(55, 0), BaseCellRotation(40, 0), BaseCellRotation(27, 3)),  // j 0
                    arrayOf(BaseCellRotation(54, 3), BaseCellRotation(36, 0), BaseCellRotation(20, 0)),  // j 1
                    arrayOf(BaseCellRotation(51, 3), BaseCellRotation(34, 0), BaseCellRotation(19, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(72, 0), BaseCellRotation(60, 3), BaseCellRotation(46, 3)),  // j 0
                    arrayOf(BaseCellRotation(73, 3), BaseCellRotation(55, 0), BaseCellRotation(40, 0)),  // j 1
                    arrayOf(BaseCellRotation(71, 3), BaseCellRotation(54, 3), BaseCellRotation(36, 0))   // j 2
                )),
            arrayOf(// face 8
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(64, 0), BaseCellRotation(47, 0), BaseCellRotation(38, 3)),  // j 0
                    arrayOf(BaseCellRotation(62, 0), BaseCellRotation(43, 3), BaseCellRotation(29, 3)),  // j 1
                    arrayOf(BaseCellRotation(58, 3), BaseCellRotation(42, 3), BaseCellRotation(26, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(84, 0), BaseCellRotation(69, 0), BaseCellRotation(51, 3)),  // j 0
                    arrayOf(BaseCellRotation(82, 3), BaseCellRotation(64, 0), BaseCellRotation(47, 0)),  // j 1
                    arrayOf(BaseCellRotation(76, 3), BaseCellRotation(62, 0), BaseCellRotation(43, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(97, 0), BaseCellRotation(89, 3), BaseCellRotation(71, 3)),  // j 0
                    arrayOf(BaseCellRotation(98, 3), BaseCellRotation(84, 0), BaseCellRotation(69, 0)),  // j 1
                    arrayOf(BaseCellRotation(96, 3), BaseCellRotation(82, 3), BaseCellRotation(64, 0))   // j 2
                )),
            arrayOf(// face 9
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(75, 0), BaseCellRotation(65, 0), BaseCellRotation(58, 3)),  // j 0
                    arrayOf(BaseCellRotation(61, 0), BaseCellRotation(53, 3), BaseCellRotation(44, 3)),  // j 1
                    arrayOf(BaseCellRotation(49, 3), BaseCellRotation(41, 3), BaseCellRotation(31, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(94, 0), BaseCellRotation(86, 0), BaseCellRotation(76, 3)),  // j 0
                    arrayOf(BaseCellRotation(81, 3), BaseCellRotation(75, 0), BaseCellRotation(65, 0)),  // j 1
                    arrayOf(BaseCellRotation(66, 3), BaseCellRotation(61, 0), BaseCellRotation(53, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(107, 0), BaseCellRotation(104, 3), BaseCellRotation(96, 3)),  // j 0
                    arrayOf(BaseCellRotation(101, 3), BaseCellRotation(94, 0), BaseCellRotation(86, 0)),   // j 1
                    arrayOf(BaseCellRotation(85, 3), BaseCellRotation(81, 3), BaseCellRotation(75, 0))     // j 2
                )),
            arrayOf(// face 10
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(57, 0), BaseCellRotation(59, 0), BaseCellRotation(63, 3)),  // j 0
                    arrayOf(BaseCellRotation(74, 0), BaseCellRotation(78, 3), BaseCellRotation(79, 3)),  // j 1
                    arrayOf(BaseCellRotation(83, 3), BaseCellRotation(92, 3), BaseCellRotation(95, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(37, 0), BaseCellRotation(39, 3), BaseCellRotation(45, 3)),  // j 0
                    arrayOf(BaseCellRotation(52, 0), BaseCellRotation(57, 0), BaseCellRotation(59, 0)),  // j 1
                    arrayOf(BaseCellRotation(70, 3), BaseCellRotation(74, 0), BaseCellRotation(78, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(24, 0), BaseCellRotation(23, 3), BaseCellRotation(25, 3)),  // j 0
                    arrayOf(BaseCellRotation(32, 3), BaseCellRotation(37, 0), BaseCellRotation(39, 3)),  // j 1
                    arrayOf(BaseCellRotation(50, 3), BaseCellRotation(52, 0), BaseCellRotation(57, 0))   // j 2
                )),
            arrayOf(// face 11
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(46, 0), BaseCellRotation(60, 0), BaseCellRotation(72, 3)),  // j 0
                    arrayOf(BaseCellRotation(56, 0), BaseCellRotation(68, 3), BaseCellRotation(80, 3)),  // j 1
                    arrayOf(BaseCellRotation(63, 3), BaseCellRotation(77, 3), BaseCellRotation(90, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(27, 0), BaseCellRotation(40, 3), BaseCellRotation(55, 3)),  // j 0
                    arrayOf(BaseCellRotation(35, 0), BaseCellRotation(46, 0), BaseCellRotation(60, 0)),  // j 1
                    arrayOf(BaseCellRotation(45, 3), BaseCellRotation(56, 0), BaseCellRotation(68, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(14, 0), BaseCellRotation(20, 3), BaseCellRotation(36, 3)),  // j 0
                    arrayOf(BaseCellRotation(17, 3), BaseCellRotation(27, 0), BaseCellRotation(40, 3)),  // j 1
                    arrayOf(BaseCellRotation(25, 3), BaseCellRotation(35, 0), BaseCellRotation(46, 0))   // j 2
                )),
            arrayOf(// face 12
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(71, 0), BaseCellRotation(89, 0), BaseCellRotation(97, 3)),   // j 0
                    arrayOf(BaseCellRotation(73, 0), BaseCellRotation(91, 3), BaseCellRotation(103, 3)),  // j 1
                    arrayOf(BaseCellRotation(72, 3), BaseCellRotation(88, 3), BaseCellRotation(105, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(51, 0), BaseCellRotation(69, 3), BaseCellRotation(84, 3)),  // j 0
                    arrayOf(BaseCellRotation(54, 0), BaseCellRotation(71, 0), BaseCellRotation(89, 0)),  // j 1
                    arrayOf(BaseCellRotation(55, 3), BaseCellRotation(73, 0), BaseCellRotation(91, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(38, 0), BaseCellRotation(47, 3), BaseCellRotation(64, 3)),  // j 0
                    arrayOf(BaseCellRotation(34, 3), BaseCellRotation(51, 0), BaseCellRotation(69, 3)),  // j 1
                    arrayOf(BaseCellRotation(36, 3), BaseCellRotation(54, 0), BaseCellRotation(71, 0))   // j 2
                )),
            arrayOf(// face 13
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(96, 0), BaseCellRotation(104, 0), BaseCellRotation(107, 3)),  // j 0
                    arrayOf(BaseCellRotation(98, 0), BaseCellRotation(110, 3), BaseCellRotation(115, 3)),  // j 1
                    arrayOf(BaseCellRotation(97, 3), BaseCellRotation(111, 3), BaseCellRotation(119, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(76, 0), BaseCellRotation(86, 3), BaseCellRotation(94, 3)),   // j 0
                    arrayOf(BaseCellRotation(82, 0), BaseCellRotation(96, 0), BaseCellRotation(104, 0)),  // j 1
                    arrayOf(BaseCellRotation(84, 3), BaseCellRotation(98, 0), BaseCellRotation(110, 3))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(58, 0), BaseCellRotation(65, 3), BaseCellRotation(75, 3)),  // j 0
                    arrayOf(BaseCellRotation(62, 3), BaseCellRotation(76, 0), BaseCellRotation(86, 3)),  // j 1
                    arrayOf(BaseCellRotation(64, 3), BaseCellRotation(82, 0), BaseCellRotation(96, 0))   // j 2
                )),
            arrayOf(// face 14
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(85, 0), BaseCellRotation(87, 0), BaseCellRotation(83, 3)),     // j 0
                    arrayOf(BaseCellRotation(101, 0), BaseCellRotation(102, 3), BaseCellRotation(100, 3)),  // j 1
                    arrayOf(BaseCellRotation(107, 3), BaseCellRotation(112, 3), BaseCellRotation(114, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(66, 0), BaseCellRotation(67, 3), BaseCellRotation(70, 3)),   // j 0
                    arrayOf(BaseCellRotation(81, 0), BaseCellRotation(85, 0), BaseCellRotation(87, 0)),   // j 1
                    arrayOf(BaseCellRotation(94, 3), BaseCellRotation(101, 0), BaseCellRotation(102, 3))  // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(49, 0), BaseCellRotation(48, 3), BaseCellRotation(50, 3)),  // j 0
                    arrayOf(BaseCellRotation(61, 3), BaseCellRotation(66, 0), BaseCellRotation(67, 3)),  // j 1
                    arrayOf(BaseCellRotation(75, 3), BaseCellRotation(81, 0), BaseCellRotation(85, 0))   // j 2
                )),
            arrayOf(// face 15
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(95, 0), BaseCellRotation(92, 0), BaseCellRotation(83, 0)),  // j 0
                    arrayOf(BaseCellRotation(79, 0), BaseCellRotation(78, 0), BaseCellRotation(74, 3)),  // j 1
                    arrayOf(BaseCellRotation(63, 1), BaseCellRotation(59, 3), BaseCellRotation(57, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(109, 0), BaseCellRotation(108, 0), BaseCellRotation(100, 5)),  // j 0
                    arrayOf(BaseCellRotation(93, 1), BaseCellRotation(95, 0), BaseCellRotation(92, 0)),     // j 1
                    arrayOf(BaseCellRotation(77, 1), BaseCellRotation(79, 0), BaseCellRotation(78, 0))      // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(117, 4), BaseCellRotation(118, 5), BaseCellRotation(114, 5)),  // j 0
                    arrayOf(BaseCellRotation(106, 1), BaseCellRotation(109, 0), BaseCellRotation(108, 0)),  // j 1
                    arrayOf(BaseCellRotation(90, 1), BaseCellRotation(93, 1), BaseCellRotation(95, 0))      // j 2
                )),
            arrayOf(// face 16
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(90, 0), BaseCellRotation(77, 0), BaseCellRotation(63, 0)),  // j 0
                    arrayOf(BaseCellRotation(80, 0), BaseCellRotation(68, 0), BaseCellRotation(56, 3)),  // j 1
                    arrayOf(BaseCellRotation(72, 1), BaseCellRotation(60, 3), BaseCellRotation(46, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(106, 0), BaseCellRotation(93, 0), BaseCellRotation(79, 5)),  // j 0
                    arrayOf(BaseCellRotation(99, 1), BaseCellRotation(90, 0), BaseCellRotation(77, 0)),   // j 1
                    arrayOf(BaseCellRotation(88, 1), BaseCellRotation(80, 0), BaseCellRotation(68, 0))    // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(117, 3), BaseCellRotation(109, 5), BaseCellRotation(95, 5)),  // j 0
                    arrayOf(BaseCellRotation(113, 1), BaseCellRotation(106, 0), BaseCellRotation(93, 0)),  // j 1
                    arrayOf(BaseCellRotation(105, 1), BaseCellRotation(99, 1), BaseCellRotation(90, 0))    // j 2
                )),
            arrayOf(// face 17
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(105, 0), BaseCellRotation(88, 0), BaseCellRotation(72, 0)),  // j 0
                    arrayOf(BaseCellRotation(103, 0), BaseCellRotation(91, 0), BaseCellRotation(73, 3)),  // j 1
                    arrayOf(BaseCellRotation(97, 1), BaseCellRotation(89, 3), BaseCellRotation(71, 3))    // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(113, 0), BaseCellRotation(99, 0), BaseCellRotation(80, 5)),   // j 0
                    arrayOf(BaseCellRotation(116, 1), BaseCellRotation(105, 0), BaseCellRotation(88, 0)),  // j 1
                    arrayOf(BaseCellRotation(111, 1), BaseCellRotation(103, 0), BaseCellRotation(91, 0))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(117, 2), BaseCellRotation(106, 5), BaseCellRotation(90, 5)),  // j 0
                    arrayOf(BaseCellRotation(121, 1), BaseCellRotation(113, 0), BaseCellRotation(99, 0)),  // j 1
                    arrayOf(BaseCellRotation(119, 1), BaseCellRotation(116, 1), BaseCellRotation(105, 0))  // j 2
                )),
            arrayOf(// face 18
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(119, 0), BaseCellRotation(111, 0), BaseCellRotation(97, 0)),  // j 0
                    arrayOf(BaseCellRotation(115, 0), BaseCellRotation(110, 0), BaseCellRotation(98, 3)),  // j 1
                    arrayOf(BaseCellRotation(107, 1), BaseCellRotation(104, 3), BaseCellRotation(96, 3))   // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(121, 0), BaseCellRotation(116, 0), BaseCellRotation(103, 5)),  // j 0
                    arrayOf(BaseCellRotation(120, 1), BaseCellRotation(119, 0), BaseCellRotation(111, 0)),  // j 1
                    arrayOf(BaseCellRotation(112, 1), BaseCellRotation(115, 0), BaseCellRotation(110, 0))   // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(117, 1), BaseCellRotation(113, 5), BaseCellRotation(105, 5)),  // j 0
                    arrayOf(BaseCellRotation(118, 1), BaseCellRotation(121, 0), BaseCellRotation(116, 0)),  // j 1
                    arrayOf(BaseCellRotation(114, 1), BaseCellRotation(120, 1), BaseCellRotation(119, 0))   // j 2
                )),
            arrayOf(// face 19
                arrayOf(
                    // i 0
                    arrayOf(BaseCellRotation(114, 0), BaseCellRotation(112, 0), BaseCellRotation(107, 0)),  // j 0
                    arrayOf(BaseCellRotation(100, 0), BaseCellRotation(102, 0), BaseCellRotation(101, 3)),  // j 1
                    arrayOf(BaseCellRotation(83, 1), BaseCellRotation(87, 3), BaseCellRotation(85, 3))      // j 2
                ),
                arrayOf(
                    // i 1
                    arrayOf(BaseCellRotation(118, 0), BaseCellRotation(120, 0), BaseCellRotation(115, 5)),  // j 0
                    arrayOf(BaseCellRotation(108, 1), BaseCellRotation(114, 0), BaseCellRotation(112, 0)),  // j 1
                    arrayOf(BaseCellRotation(92, 1), BaseCellRotation(100, 0), BaseCellRotation(102, 0))    // j 2
                ),
                arrayOf(
                    // i 2
                    arrayOf(BaseCellRotation(117, 0), BaseCellRotation(121, 5), BaseCellRotation(119, 5)),  // j 0
                    arrayOf(BaseCellRotation(109, 1), BaseCellRotation(118, 0), BaseCellRotation(120, 0)),  // j 1
                    arrayOf(BaseCellRotation(95, 1), BaseCellRotation(108, 1), BaseCellRotation(114, 0))    // j 2
                )))

        internal val baseCellData = arrayOf(
            BaseCellData(FaceIJK(1, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 0
            BaseCellData(FaceIJK(2, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),     // base cell 1
            BaseCellData(FaceIJK(1, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 2
            BaseCellData(FaceIJK(2, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 3
            BaseCellData(FaceIJK(0, CoordIJK(2, 0, 0)), true, intArrayOf(-1, -1)),   // base cell 4
            BaseCellData(FaceIJK(1, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),     // base cell 5
            BaseCellData(FaceIJK(1, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 6
            BaseCellData(FaceIJK(2, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 7
            BaseCellData(FaceIJK(0, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 8
            BaseCellData(FaceIJK(2, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 9
            BaseCellData(FaceIJK(1, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 10
            BaseCellData(FaceIJK(1, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),     // base cell 11
            BaseCellData(FaceIJK(3, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 12
            BaseCellData(FaceIJK(3, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),     // base cell 13
            BaseCellData(FaceIJK(11, CoordIJK(2, 0, 0)), true, intArrayOf(2, 6)),    // base cell 14
            BaseCellData(FaceIJK(4, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 15
            BaseCellData(FaceIJK(0, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 16
            BaseCellData(FaceIJK(6, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 17
            BaseCellData(FaceIJK(0, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 18
            BaseCellData(FaceIJK(2, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),     // base cell 19
            BaseCellData(FaceIJK(7, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 20
            BaseCellData(FaceIJK(2, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 21
            BaseCellData(FaceIJK(0, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),     // base cell 22
            BaseCellData(FaceIJK(6, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 23
            BaseCellData(FaceIJK(10, CoordIJK(2, 0, 0)), true, intArrayOf(1, 5)),    // base cell 24
            BaseCellData(FaceIJK(6, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 25
            BaseCellData(FaceIJK(3, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 26
            BaseCellData(FaceIJK(11, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 27
            BaseCellData(FaceIJK(4, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),     // base cell 28
            BaseCellData(FaceIJK(3, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 29
            BaseCellData(FaceIJK(0, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),     // base cell 30
            BaseCellData(FaceIJK(4, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 31
            BaseCellData(FaceIJK(5, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 32
            BaseCellData(FaceIJK(0, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 33
            BaseCellData(FaceIJK(7, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 34
            BaseCellData(FaceIJK(11, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),    // base cell 35
            BaseCellData(FaceIJK(7, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 36
            BaseCellData(FaceIJK(10, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 37
            BaseCellData(FaceIJK(12, CoordIJK(2, 0, 0)), true, intArrayOf(3, 7)),    // base cell 38
            BaseCellData(FaceIJK(6, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),     // base cell 39
            BaseCellData(FaceIJK(7, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),     // base cell 40
            BaseCellData(FaceIJK(4, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 41
            BaseCellData(FaceIJK(3, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 42
            BaseCellData(FaceIJK(3, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),     // base cell 43
            BaseCellData(FaceIJK(4, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 44
            BaseCellData(FaceIJK(6, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 45
            BaseCellData(FaceIJK(11, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 46
            BaseCellData(FaceIJK(8, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 47
            BaseCellData(FaceIJK(5, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 48
            BaseCellData(FaceIJK(14, CoordIJK(2, 0, 0)), true, intArrayOf(0, 9)),    // base cell 49
            BaseCellData(FaceIJK(5, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 50
            BaseCellData(FaceIJK(12, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 51
            BaseCellData(FaceIJK(10, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),    // base cell 52
            BaseCellData(FaceIJK(4, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),     // base cell 53
            BaseCellData(FaceIJK(12, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),    // base cell 54
            BaseCellData(FaceIJK(7, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 55
            BaseCellData(FaceIJK(11, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 56
            BaseCellData(FaceIJK(10, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 57
            BaseCellData(FaceIJK(13, CoordIJK(2, 0, 0)), true, intArrayOf(4, 8)),    // base cell 58
            BaseCellData(FaceIJK(10, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 59
            BaseCellData(FaceIJK(11, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 60
            BaseCellData(FaceIJK(9, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 61
            BaseCellData(FaceIJK(8, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),     // base cell 62
            BaseCellData(FaceIJK(6, CoordIJK(2, 0, 0)), true, intArrayOf(11, 15)),   // base cell 63
            BaseCellData(FaceIJK(8, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 64
            BaseCellData(FaceIJK(9, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),     // base cell 65
            BaseCellData(FaceIJK(14, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 66
            BaseCellData(FaceIJK(5, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),     // base cell 67
            BaseCellData(FaceIJK(16, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),    // base cell 68
            BaseCellData(FaceIJK(8, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),     // base cell 69
            BaseCellData(FaceIJK(5, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 70
            BaseCellData(FaceIJK(12, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 71
            BaseCellData(FaceIJK(7, CoordIJK(2, 0, 0)), true, intArrayOf(12, 16)),   // base cell 72
            BaseCellData(FaceIJK(12, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 73
            BaseCellData(FaceIJK(10, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 74
            BaseCellData(FaceIJK(9, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),     // base cell 75
            BaseCellData(FaceIJK(13, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 76
            BaseCellData(FaceIJK(16, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 77
            BaseCellData(FaceIJK(15, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),    // base cell 78
            BaseCellData(FaceIJK(15, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 79
            BaseCellData(FaceIJK(16, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 80
            BaseCellData(FaceIJK(14, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),    // base cell 81
            BaseCellData(FaceIJK(13, CoordIJK(1, 1, 0)), false, intArrayOf(0, 0)),    // base cell 82
            BaseCellData(FaceIJK(5, CoordIJK(2, 0, 0)), true, intArrayOf(10, 19)),   // base cell 83
            BaseCellData(FaceIJK(8, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 84
            BaseCellData(FaceIJK(14, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 85
            BaseCellData(FaceIJK(9, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),     // base cell 86
            BaseCellData(FaceIJK(14, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 87
            BaseCellData(FaceIJK(17, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 88
            BaseCellData(FaceIJK(12, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 89
            BaseCellData(FaceIJK(16, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 90
            BaseCellData(FaceIJK(17, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),    // base cell 91
            BaseCellData(FaceIJK(15, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 92
            BaseCellData(FaceIJK(16, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),    // base cell 93
            BaseCellData(FaceIJK(9, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),     // base cell 94
            BaseCellData(FaceIJK(15, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 95
            BaseCellData(FaceIJK(13, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 96
            BaseCellData(FaceIJK(8, CoordIJK(2, 0, 0)), true, intArrayOf(13, 17)),   // base cell 97
            BaseCellData(FaceIJK(13, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 98
            BaseCellData(FaceIJK(17, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),    // base cell 99
            BaseCellData(FaceIJK(19, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 100
            BaseCellData(FaceIJK(14, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 101
            BaseCellData(FaceIJK(19, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),    // base cell 102
            BaseCellData(FaceIJK(17, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 103
            BaseCellData(FaceIJK(13, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 104
            BaseCellData(FaceIJK(17, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 105
            BaseCellData(FaceIJK(16, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 106
            BaseCellData(FaceIJK(9, CoordIJK(2, 0, 0)), true, intArrayOf(14, 18)),   // base cell 107
            BaseCellData(FaceIJK(15, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),    // base cell 108
            BaseCellData(FaceIJK(15, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 109
            BaseCellData(FaceIJK(18, CoordIJK(0, 1, 1)), false, intArrayOf(0, 0)),    // base cell 110
            BaseCellData(FaceIJK(18, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 111
            BaseCellData(FaceIJK(19, CoordIJK(0, 0, 1)), false, intArrayOf(0, 0)),    // base cell 112
            BaseCellData(FaceIJK(17, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 113
            BaseCellData(FaceIJK(19, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 114
            BaseCellData(FaceIJK(18, CoordIJK(0, 1, 0)), false, intArrayOf(0, 0)),    // base cell 115
            BaseCellData(FaceIJK(18, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),    // base cell 116
            BaseCellData(FaceIJK(19, CoordIJK(2, 0, 0)), true, intArrayOf(-1, -1)),  // base cell 117
            BaseCellData(FaceIJK(19, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0)),    // base cell 118
            BaseCellData(FaceIJK(18, CoordIJK(0, 0, 0)), false, intArrayOf(0, 0)),    // base cell 119
            BaseCellData(FaceIJK(19, CoordIJK(1, 0, 1)), false, intArrayOf(0, 0)),    // base cell 120
            BaseCellData(FaceIJK(18, CoordIJK(1, 0, 0)), false, intArrayOf(0, 0))     // base cell 121
        )

        internal fun isBaseCellPentagon(baseCell: Int): Boolean {
            if(baseCell < 0 || baseCell >= NUM_BASE_CELLS) {
                return false
            }
            return baseCellData[baseCell].isPentagon
        }

        internal fun baseCellIsCwOffset(baseCell: Int, testFace: Int): Boolean {
            return baseCellData[baseCell].cwOffsetPent[0] == testFace || baseCellData[baseCell].cwOffsetPent[1] == testFace
        }
    }
}

class FaceIJK (var face: Int = 0, var coord: CoordIJK = CoordIJK()) {

    internal fun faceIjkToBaseCell(): Int {
        return BaseCells.faceIjkBaseCells[face][coord.i][coord.j][coord.k].baseCell
    }

    internal fun faceIjkToBaseCellCCWrot60(): Int {
        return BaseCells.faceIjkBaseCells[face][coord.i][coord.j][coord.k].ccwRot60
    }

    internal fun faceIjkToH3(res: Int): H3Index {
        var h = H3.H3_INIT
        h = H3.h3SetMode(h, H3_CELL_MODE)
        h = H3.h3SetResolution(h, res)

        if(res == 0) {
            if(coord.i > MAX_FACE_COORD || coord.j > MAX_FACE_COORD || coord.k > MAX_FACE_COORD) {
                return H3.H3_NULL
            }

            h = H3.h3SetBaseCell(h, faceIjkToBaseCell())
            return h
        }

        val fijkBC = FaceIJK(face, CoordIJK(coord.i, coord.j, coord.k))
        val ijk = fijkBC.coord
        for(r in (res - 1) downTo 0) {
            val lastIJK = CoordIJK(ijk.i, ijk.j, ijk.k)
            val lastCenter: CoordIJK
            if(H3.isResolutionClassIII(r + 1)) {
                ijk.upAp7()
                lastCenter = CoordIJK(ijk.i, ijk.j, ijk.k)
                lastCenter.downAp7()
            } else {
                ijk.upAp7r()
                lastCenter = CoordIJK(ijk.i, ijk.j, ijk.k)
                lastCenter.downAp7r()
            }

            val diff = lastIJK.ijkSub(lastCenter)
            diff.ijkNormalize()

            h = H3.h3SetIndexDigit(h, r + 1, diff.unitIjkToDigit())
        }

        if(fijkBC.coord.i > MAX_FACE_COORD || fijkBC.coord.j > MAX_FACE_COORD || fijkBC.coord.k > MAX_FACE_COORD) {
            return H3.H3_NULL
        }

        val baseCell = fijkBC.faceIjkToBaseCell()
        h = H3.h3SetBaseCell(h, baseCell)

        val numRots = fijkBC.faceIjkToBaseCellCCWrot60()
        if(BaseCells.isBaseCellPentagon(baseCell)) {
            if(H3.h3LeadingNonZeroDigit(h) == K_AXES_DIGIT) {
                if(BaseCells.baseCellIsCwOffset(baseCell, fijkBC.face)) {
                    h = H3.h3Rotate60cw(h)
                } else {
                    h = H3.h3Rotate60ccw(h)
                }
            }

            for(i in 0..(numRots-1)) h = H3.h3RotatePent60ccw(h)
        } else {
            for(i in 0..(numRots-1)) h = H3.h3Rotate60ccw(h)
        }

        return h
    }

    companion object {
        internal val faceCenterGeo = arrayOf(
            LatLng(0.803582649718989942, 1.248397419617396099),    // face  0
            LatLng(1.307747883455638156, 2.536945009877921159),    // face  1
            LatLng(1.054751253523952054, -1.347517358900396623),   // face  2
            LatLng(0.600191595538186799, -0.450603909469755746),   // face  3
            LatLng(0.491715428198773866, 0.401988202911306943),    // face  4
            LatLng(0.172745327415618701, 1.678146885280433686),    // face  5
            LatLng(0.605929321571350690, 2.953923329812411617),    // face  6
            LatLng(0.427370518328979641, -1.888876200336285401),   // face  7
            LatLng(-0.079066118549212831, -0.733429513380867741),  // face  8
            LatLng(-0.230961644455383637, 0.506495587332349035),   // face  9
            LatLng(0.079066118549212831, 2.408163140208925497),    // face 10
            LatLng(0.230961644455383637, -2.635097066257444203),   // face 11
            LatLng(-0.172745327415618701, -1.463445768309359553),  // face 12
            LatLng(-0.605929321571350690, -0.187669323777381622),  // face 13
            LatLng(-0.427370518328979641, 1.252716453253507838),   // face 14
            LatLng(-0.600191595538186799, 2.690988744120037492),   // face 15
            LatLng(-0.491715428198773866, -2.739604450678486295),  // face 16
            LatLng(-0.803582649718989942, -1.893195233972397139),  // face 17
            LatLng(-1.307747883455638156, -0.604647643711872080),  // face 18
            LatLng(-1.054751253523952054, 1.794075294689396615),   // face 19
        )

        internal val faceCenterPoint = arrayOf(
            Vec3d(0.2199307791404606, 0.6583691780274996, 0.7198475378926182),     // face  0
            Vec3d(-0.2139234834501421, 0.1478171829550703, 0.9656017935214205),    // face  1
            Vec3d(0.1092625278784797, -0.4811951572873210, 0.8697775121287253),    // face  2
            Vec3d(0.7428567301586791, -0.3593941678278028, 0.5648005936517033),    // face  3
            Vec3d(0.8112534709140969, 0.3448953237639384, 0.4721387736413930),     // face  4
            Vec3d(-0.1055498149613921, 0.9794457296411413, 0.1718874610009365),    // face  5
            Vec3d(-0.8075407579970092, 0.1533552485898818, 0.5695261994882688),    // face  6
            Vec3d(-0.2846148069787907, -0.8644080972654206, 0.4144792552473539),   // face  7
            Vec3d(0.7405621473854482, -0.6673299564565524, -0.0789837646326737),   // face  8
            Vec3d(0.8512303986474293, 0.4722343788582681, -0.2289137388687808),    // face  9
            Vec3d(-0.7405621473854481, 0.6673299564565524, 0.0789837646326737),    // face 10
            Vec3d(-0.8512303986474292, -0.4722343788582682, 0.2289137388687808),   // face 11
            Vec3d(0.1055498149613919, -0.9794457296411413, -0.1718874610009365),   // face 12
            Vec3d(0.8075407579970092, -0.1533552485898819, -0.5695261994882688),   // face 13
            Vec3d(0.2846148069787908, 0.8644080972654204, -0.4144792552473539),    // face 14
            Vec3d(-0.7428567301586791, 0.3593941678278027, -0.5648005936517033),   // face 15
            Vec3d(-0.811253470914097, -0.3448953237639382, -0.4721387736413930),  // face 16
            Vec3d(-0.2199307791404607, -0.6583691780274996, -0.7198475378926182),  // face 17
            Vec3d(0.2139234834501420, -0.1478171829550704, -0.9656017935214205),   // face 18
            Vec3d(-0.1092625278784796, 0.4811951572873210, -0.8697775121287253),   // face 19
        )

        internal val faceAxesAzRadsCII = arrayOf(
            arrayOf(5.619958268523939882, 3.525563166130744542,
                1.431168063737548730),  // face  0
            arrayOf(5.760339081714187279, 3.665943979320991689,
                1.571548876927796127),  // face  1
            arrayOf(0.780213654393430055, 4.969003859179821079,
                2.874608756786625655),  // face  2
            arrayOf(0.430469363979999913, 4.619259568766391033,
                2.524864466373195467),  // face  3
            arrayOf(6.130269123335111400, 4.035874020941915804,
                1.941478918548720291),  // face  4
            arrayOf(2.692877706530642877, 0.598482604137447119,
                4.787272808923838195),  // face  5
            arrayOf(2.982963003477243874, 0.888567901084048369,
                5.077358105870439581),  // face  6
            arrayOf(3.532912002790141181, 1.438516900396945656,
                5.627307105183336758),  // face  7
            arrayOf(3.494305004259568154, 1.399909901866372864,
                5.588700106652763840),  // face  8
            arrayOf(3.003214169499538391, 0.908819067106342928,
                5.097609271892733906),  // face  9
            arrayOf(5.930472956509811562, 3.836077854116615875,
                1.741682751723420374),  // face 10
            arrayOf(0.138378484090254847, 4.327168688876645809,
                2.232773586483450311),  // face 11
            arrayOf(0.448714947059150361, 4.637505151845541521,
                2.543110049452346120),  // face 12
            arrayOf(0.158629650112549365, 4.347419854898940135,
                2.253024752505744869),  // face 13
            arrayOf(5.891865957979238535, 3.797470855586042958,
                1.703075753192847583),  // face 14
            arrayOf(2.711123289609793325, 0.616728187216597771,
                4.805518392002988683),  // face 15
            arrayOf(3.294508837434268316, 1.200113735041072948,
                5.388903939827463911),  // face 16
            arrayOf(3.804819692245439833, 1.710424589852244509,
                5.899214794638635174),  // face 17
            arrayOf(3.664438879055192436, 1.570043776661997111,
                5.758833981448388027),  // face 18
            arrayOf(2.361378999196363184, 0.266983896803167583,
                4.455774101589558636),  // face 19
        )
    }

}

typealias H3Index = ULong

class H3 () {

    companion object {
        fun h3toString(h: H3Index): String {
            return h.toString(16)
        }
        internal fun isResolutionClassIII(res: Int): Boolean {
            return res % 2 != 0
        }

        internal const val H3_NUM_BITS = 64
        internal const val H3_MAX_OFFSET = 63

        internal const val H3_MODE_OFFSET = 59
        internal const val H3_BC_OFFSET = 45
        internal const val H3_RES_OFFSET = 52
        internal const val H3_RESERVED_OFFSET = 56
        internal const val H3_PER_DIGIT_OFFSET = 3

        internal val H3_HIGH_BIT_MASK = 1UL shl H3_MAX_OFFSET
        internal val H3_HIGH_BIT_MASK_NEGATIVE = H3_HIGH_BIT_MASK.inv()
        internal val H3_MODE_MASK = 15UL shl H3_MODE_OFFSET
        internal val H3_MODE_MASK_NEGATIVE = H3_MODE_MASK.inv()
        internal val H3_BC_MASK = 127UL shl H3_BC_OFFSET
        internal val H3_BC_MASK_NEGATIVE = H3_BC_MASK.inv()
        internal val H3_RES_MASK = 15UL shl H3_RES_OFFSET
        internal val H3_RES_MASK_NEGATIVE = H3_RES_MASK.inv()
        internal val H3_RESERVED_MASK = 7UL shl H3_RESERVED_OFFSET
        internal val H3_RESERVED_MASK_NEGATIVE = H3_RESERVED_MASK.inv()
        internal val H3_DIGIT_MASK = 7UL
        internal val H3_DIGIT_MASK_NEGATIVE = H3_DIGIT_MASK.inv()

        internal const val H3_INIT = 35184372088831UL
        internal const val H3_NULL = 0UL

        internal fun h3GetHighBit(h3: H3Index): Int {
            return ((h3 and H3_HIGH_BIT_MASK) shr H3_MAX_OFFSET).toInt()
        }

        internal fun h3SetHighBit(h3: H3Index, v: Int): H3Index {
            return ((h3 and H3_HIGH_BIT_MASK_NEGATIVE) or (v.toULong() shl H3_MAX_OFFSET))
        }

        internal fun h3GetMode(h3: H3Index): Int {
            return ((h3 and H3_MODE_MASK) shr H3_MODE_OFFSET).toInt()
        }

        internal fun h3SetMode(h3: H3Index, v: Int): H3Index {
            return ((h3 and H3_MODE_MASK_NEGATIVE) or (v.toULong() shl H3_MODE_OFFSET))
        }

        internal fun h3GetBaseCell(h3: H3Index): Int {
            return ((h3 and H3_BC_MASK) shr H3_BC_OFFSET).toInt()
        }

        internal fun h3SetBaseCell(h3: H3Index, v: Int): H3Index {
            return ((h3 and H3_BC_MASK_NEGATIVE) or (v.toULong() shl H3_BC_OFFSET))
        }

        internal fun h3GetResolution(h3: H3Index): Int {
            return ((h3 and H3_RES_MASK) shr H3_RES_OFFSET).toInt()
        }

        internal fun h3SetResolution(h3: H3Index, v: Int): H3Index {
            return ((h3 and H3_RES_MASK_NEGATIVE) or (v.toULong() shl H3_RES_OFFSET))
        }

        internal fun h3GetIndexDigit(h3: H3Index, res: Int): Direction {
            return ((h3 shr ((MAX_H3_RES - res) * H3_PER_DIGIT_OFFSET )) and H3_DIGIT_MASK).toInt()
        }

        internal fun h3SetIndexDigit(h3: H3Index, res: Int, digit: Int): H3Index {
            return (h3 and (H3_DIGIT_MASK shl ((MAX_H3_RES - res) * H3_PER_DIGIT_OFFSET)).inv()) or (digit.toULong() shl ((MAX_H3_RES - res) * H3_PER_DIGIT_OFFSET))
        }

        internal fun h3LeadingNonZeroDigit(h: H3Index): Direction {
            for(r in 1..h3GetResolution(h)) {
                if(h3GetIndexDigit(h, r) != 0) return h3GetIndexDigit(h, r)
            }
            return CENTER_DIGIT
        }

        internal fun rotate60ccw(digit: Direction): Direction {
            return when(digit) {
                K_AXES_DIGIT -> IK_AXES_DIGIT
                IK_AXES_DIGIT -> I_AXES_DIGIT
                I_AXES_DIGIT -> IJ_AXES_DIGIT
                IJ_AXES_DIGIT -> J_AXES_DIGIT
                J_AXES_DIGIT -> JK_AXES_DIGIT
                JK_AXES_DIGIT -> K_AXES_DIGIT
                else -> digit
            }
        }

        internal fun rotate60cw(digit: Direction): Direction {
            return when(digit) {
                K_AXES_DIGIT -> JK_AXES_DIGIT
                JK_AXES_DIGIT -> J_AXES_DIGIT
                J_AXES_DIGIT -> IJ_AXES_DIGIT
                IJ_AXES_DIGIT -> I_AXES_DIGIT
                I_AXES_DIGIT -> IK_AXES_DIGIT
                IK_AXES_DIGIT -> K_AXES_DIGIT
                else -> digit
            }
        }
        internal fun h3Rotate60ccw(h: H3Index): H3Index {
            var hRotated = h
            val res = h3GetResolution(hRotated)
            for(r in 1..res) {
                val oldDigit = h3GetIndexDigit(hRotated, r)
                hRotated = h3SetIndexDigit(hRotated, r, rotate60ccw(oldDigit))
            }
            return hRotated
        }

        internal fun h3Rotate60cw(h: H3Index): H3Index {
            var hRotated = h
            val res = h3GetResolution(h)
            for(r in 1..res) {
                val oldDigit = h3GetIndexDigit(hRotated, r)
                hRotated = h3SetIndexDigit(hRotated, r, rotate60cw(oldDigit))
            }
            return hRotated
        }

        internal fun h3RotatePent60ccw(h: H3Index): H3Index {
            var hRotated = h
            var foundFirstNonZeroDigit = false
            val res = h3GetResolution(hRotated)
            for(r in 1..res) {
                hRotated = h3SetIndexDigit(hRotated, r, rotate60ccw(h3GetIndexDigit(hRotated, r)))

                if(!foundFirstNonZeroDigit && h3GetIndexDigit(hRotated, r) != 0) {
                    foundFirstNonZeroDigit = true

                    if(h3LeadingNonZeroDigit(hRotated) == K_AXES_DIGIT)
                        hRotated = h3Rotate60ccw(hRotated)
                }
            }
            return hRotated
        }

        internal fun h3RotatePent60cw(h: H3Index): H3Index {
            var hRotated = h
            var foundFirstNonZeroDigit = false
            val res = h3GetResolution(hRotated)
            for(r in 1..res) {
                hRotated = h3SetIndexDigit(hRotated, r, rotate60cw(h3GetIndexDigit(hRotated, r)))

                if(!foundFirstNonZeroDigit && h3GetIndexDigit(hRotated, r) != 0) {
                    foundFirstNonZeroDigit = true

                    if(h3LeadingNonZeroDigit(hRotated) == K_AXES_DIGIT)
                        hRotated = h3Rotate60cw(hRotated)
                }
            }
            return hRotated
        }
    }
}

