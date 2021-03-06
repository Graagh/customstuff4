package cubex2.cs4.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

public class CollectionHelper
{
    public static <T,U> boolean equalsWithoutOrder(List<T> fst, List<U> snd, BiPredicate<T, U> equality)
    {
        if (fst != null && snd != null)
        {
            if (fst.size() == snd.size())
            {
                // create copied lists so the original list is not modified
                List<T> cfst = new ArrayList<>(fst);
                List<U> csnd = new ArrayList<>(snd);

                Iterator<T> ifst = cfst.iterator();
                boolean foundEqualObject;
                while (ifst.hasNext())
                {
                    T a = ifst.next();
                    Iterator<U> isnd = csnd.iterator();
                    foundEqualObject = false;
                    while (isnd.hasNext())
                    {
                        U b = isnd.next();
                        if (equality.test(a, b))
                        {
                            ifst.remove();
                            isnd.remove();
                            foundEqualObject = true;
                            break;
                        }
                    }

                    if (!foundEqualObject)
                    {
                        // fail early
                        break;
                    }
                }
                if (cfst.isEmpty())
                { //both temporary lists have the same size
                    return true;
                }
            }
        } else if (fst == null && snd == null)
        {
            return true;
        }
        return false;
    }
}
