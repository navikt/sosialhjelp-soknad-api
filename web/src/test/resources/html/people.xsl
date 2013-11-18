<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <div>
            <h2>People</h2>
            <table border="1">
                <tr>
                    <th>Person</th>
                    <th>Age</th>
                </tr>
                <xsl:for-each select="people/person">
                    <tr>
                        <td>
                            <xsl:value-of select="name"/>
                        </td>
                        <td>
                            <xsl:value-of select="age"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

    <xsl:output method="html" encoding="UTF-8"/>
</xsl:stylesheet>

