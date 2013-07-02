/*
 * Coordinate Transformations Suite (abridged CTS)  is a library developped to 
 * perform Coordinate Transformations using well known geodetic algorithms 
 * and parameter sets. 
 * Its main focus are simplicity, flexibility, interoperability, in this order.
 *
 * This library has been originally developed by Michaël Michaud under the JGeod
 * name. It has been renamed CTS in 2009 and shared to the community from 
 * the Atelier SIG code repository.
 * 
 * Since them, CTS is supported by the Atelier SIG team in collaboration with Michaël 
 * Michaud.
 * The new CTS has been funded  by the French Agence Nationale de la Recherche 
 * (ANR) under contract ANR-08-VILL-0005-01 and the regional council 
 * "Région Pays de La Loire" under the projet SOGVILLE (Système d'Orbservation 
 * Géographique de la Ville).
 *
 * CTS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * CTS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * CTS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <https://github.com/irstv/cts/>
 */
package org.cts.datum;

import java.util.*;
import org.cts.op.CoordinateOperation;
import org.cts.IdentifiableComponent;
import org.cts.Identifier;
import org.cts.cs.Extent;
import org.cts.op.CoordinateOperationSequence;
import org.cts.op.NonInvertibleOperationException;

/**
 * A datum (plural datums) is a reference from which measurements are made.<p>
 * In surveying and geodesy, a datum is a reference point on the earth's surface
 * against which position measurements are made, and an associated model of the
 * shape of the earth for computing positions. Horizontal datums are used for
 * describing a point on the earth's surface, in latitude and longitude or
 * another coordinate system. Vertical datums are used to measure elevations or
 * underwater depths. In engineering and drafting, a datum is a reference point,
 * surface, or axis on an object against which measurements are made.<p> (Taken
 * from <a ref="http://en.wikipedia.org/wiki/Datum">wikipedia</a> on
 * 2006-10-06)</p>
 *
 * @author Michaël Michaud
 */
public abstract class AbstractDatum extends IdentifiableComponent
        implements Datum {

    private Extent extent;
    private String origin;
    private String epoch;
    // A map of known transformations from this datum to other datums
    private Map<Datum, List<CoordinateOperation>> datumTransformations =
            new HashMap<Datum, List<CoordinateOperation>>();

    /**
     * Creates a new Datum.
     *
     * @param identifier
     * @param extent valid domain extent (extent definition depends on the kind
     * of Datum)
     * @param origin description of the origin or anchor point of this Datum.
     * @param epoch epoch of this Datum realization
     */
    protected AbstractDatum(Identifier identifier, Extent extent, String origin, String epoch) {
        super(identifier);
        this.extent = extent;
        this.origin = origin;
        this.epoch = epoch;
    }

    /**
     * Returns the valid extent of this Datum
     */
    @Override
    public Extent getExtent() {
        return extent;
    }

    /**
     * Returns the description of this Datum origin
     */
    @Override
    public String getOrigin() {
        return origin;
    }

    /**
     * Returns the realization epoch of this Datum as a String
     */
    @Override
    public String getEpoch() {
        return epoch;
    }

    /**
     * Add a Transformation to another Datum.
     */
    public void addCoordinateOperation(Datum datum, CoordinateOperation coordOp) {
        if (datumTransformations.get(datum) == null) {
            datumTransformations.put(datum, new ArrayList<CoordinateOperation>());
        }
        datumTransformations.get(datum).add(coordOp);
    }

    /**
     * Get a transformation to another datum.
     */
    public List<CoordinateOperation> getCoordinateOperations(Datum datum) {
        if (datumTransformations.get(datum) == null) {
            if (!getCoordinateOperations(GeodeticDatum.WGS84).isEmpty() && !GeodeticDatum.WGS84.getCoordinateOperations(datum).isEmpty()) {
                CoordinateOperation op = new CoordinateOperationSequence(new Identifier(CoordinateOperationSequence.class, getName() + "to" + datum.getName() + "throughWGS84"),
                        getCoordinateOperations(GeodeticDatum.WGS84).get(0),
                        GeodeticDatum.WGS84.getCoordinateOperations(datum).get(0));
                addCoordinateOperation(datum, op);
                try {
                    ((AbstractDatum) datum).addCoordinateOperation(datum, op.inverse());
                } catch (NonInvertibleOperationException e) {
                    /* The geocentric transformation should always be inversible.
                     * Moreover, add the transformation to the target datum is useful
                     * for further calulation but not essential, so if the inversion
                     * fails it has no importance
                     */
                }
            } else {
                datumTransformations.put(datum, new ArrayList<CoordinateOperation>());
            }
        }
        return datumTransformations.get(datum);
    }

    /**
     * Returns a String representation of this Datum.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getIdentifier().toString());
        sb.append(" [");
        for (Iterator<Datum> it = datumTransformations.keySet().iterator(); it.hasNext();) {
            sb.append("").append(it.next().getShortName());
            if (it.hasNext()) {
                sb.append(" - ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
