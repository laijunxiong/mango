package cc.concurrent.mango.operator;

import cc.concurrent.mango.exception.EmptyParameterException;
import cc.concurrent.mango.exception.NullParameterException;
import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.*;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BatchUpdateOperator.class);

    private ASTRootNode rootNode;

    private BatchUpdateOperator(ASTRootNode rootNode, Method method) {
        this.rootNode = rootNode;
        init(method);
    }

    public void init(Method method) {
        buildCacheDescriptor(method);
        checkType(method.getGenericParameterTypes());
    }

    public static BatchUpdateOperator create(ASTRootNode rootNode, Method method) {
        return new BatchUpdateOperator(rootNode, method);
    }

    @Override
    public void checkType(Type[] methodArgTypes) {
        Type type = methodArgTypes[0];
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null) {
            throw new RuntimeException(""); // TODO
        }
        if (JdbcUtils.isSingleColumnClass(mappedClass)) {
            throw new RuntimeException(""); // TODO
        }
        if (!typeToken.isIterable()) {
            throw new RuntimeException(""); // TODO
        }
        Map<String, Type> parameterTypeMap = Maps.newHashMap();
        parameterTypeMap.put(String.valueOf(1), mappedClass);
        TypeContextImpl context = new TypeContextImpl(parameterTypeMap);
        rootNode.checkType(context);
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Object methodArg = methodArgs[0];
        if (methodArg == null) {
            throw new NullParameterException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(methodArg);
        if (!iterables.isIterable()) {
            throw new IncorrectParameterTypeException("expected collection or array but " + methodArg.getClass());
        }
        if (iterables.isEmpty()) {
            throw new EmptyParameterException("batchUpdate's parameter can't be empty");
        }

        Set<String> keys = Sets.newHashSet();
        boolean isUseCache = cacheDescriptor.isUseCache();
        List<Object[]> batchArgs = Lists.newArrayList();
        String sql = null;
        for (Object obj : iterables) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("1", obj);
            RuntimeContext context = new RuntimeContextImpl(parameters);
            if (isUseCache) {
                keys.add(getSingleKey(context));
            }
            ParsedSql parsedSql= rootNode.buildSqlAndArgs(context);
            if (sql == null) {
                sql = parsedSql.getSql();
            }
            batchArgs.add(parsedSql.getArgs());
        }
        if (logger.isDebugEnabled()) {
            List<String> str = Lists.newArrayList();
            for (Object[] args : batchArgs) {
                str.add(Arrays.toString(args));
            }
            logger.debug(Objects.toStringHelper("BatchUpdateOperator").add("sql", sql).add("batchArgs", str).toString());
        }
        int[] ints = jdbcTemplate.batchUpdate(sql, batchArgs);
        if (isUseCache) {
            dataCache.delete(keys);
        }
        return ints;
    }

}
