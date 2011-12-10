package siena.base.test.model;

import siena.*;

/**
 * User: Wursteisen David
 * Date: 10/12/11
 * Time: 13:15
 */
@Table("text_model")
public class TextModel extends Model {
    
    @Id(Generator.AUTO_INCREMENT)
    public Long id;
    
    @Text
    public String text;
}
