package ch.difty.scipamato.core.entity;

import lombok.*;

import ch.difty.scipamato.common.entity.FieldEnumType;

/**
 * SciPaMaTo entity having a numeric id.
 *
 * @param <ID>
 *     type of the numeric id
 * @author u.joss
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class IdScipamatoEntity<ID extends Number> extends CoreEntity {

    private static final long serialVersionUID = 1L;

    private ID id;

    public enum IdScipamatoEntityFields implements FieldEnumType {
        ID("id");

        private final String name;

        IdScipamatoEntityFields(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

}
