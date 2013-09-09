/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.tools.archives.scanner.internal;

import java.io.File;

/**
 * A list of patterns to be matched
 * 
 * Class History:
 * 		Taken from PlexUtils 3.3.1
 * 		All unused methods for our use case were removed. 
 *
 *
 * @author Kristian Rosenvold
 */
public class MatchPatterns
{
    private final MatchPattern[] patterns;

    private MatchPatterns( MatchPattern[] patterns )
    {
        this.patterns = patterns;
    }

    /**
     * Checks these MatchPatterns against a specified string.
     * <p/>
     * Uses far less string tokenization than any of the alternatives.
     *
     * @param name            The name to look for
     * @param isCaseSensitive If the comparison is case sensitive
     * @return true if any of the supplied patterns match
     */
    public boolean matches( String name, boolean isCaseSensitive )
    {
        String[] tokenized = MatchPattern.tokenizePathToString( name, File.separator );
        return matches(  name, tokenized, isCaseSensitive );
    }

    public boolean matches( String name, String[] tokenizedName, boolean isCaseSensitive )
    {
        char[][] tokenizedNameChar = new char[tokenizedName.length][];
        for(int i = 0;  i < tokenizedName.length; i++){
        tokenizedNameChar[i] = tokenizedName[i].toCharArray();
        }
        for ( MatchPattern pattern : patterns )
        {
            if ( pattern.matchPath( name, tokenizedNameChar, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean matchesPatternStart( String name, boolean isCaseSensitive )
    {
        for ( MatchPattern includesPattern : patterns )
        {
            if ( includesPattern.matchPatternStart( name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    public static MatchPatterns from( String... sources )
    {
        final int length = sources.length;
        MatchPattern[] result = new MatchPattern[length];
        for ( int i = 0; i < length; i++ )
        {
            result[i] = MatchPattern.fromString( sources[i] );
        }
        return new MatchPatterns( result );
    }
}
