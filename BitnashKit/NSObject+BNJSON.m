//
//  NSObject+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "NSObject+BNJSON.h"

@implementation NSObject (BNJSON)

- (id)asJSONObject
{
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wundeclared-selector"
    if ([[self class] respondsToSelector:@selector(jsonProperties)])
#pragma clang diagnostic pop
    {
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wundeclared-selector"
        for (NSString *property in [[self class] performSelector:@selector(jsonProperties)])
#pragma clang diagnostic pop
        {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
            id object = [self performSelector:NSSelectorFromString(property)];
            if (object == nil)
            {
                object = [NSNull null];
            }
            [dict setObject:[object asJSONObject] forKey:property];
#pragma clang diagnostic pop
        }
        return dict;
    }
    else
    {
        return self;
    }
}

- (NSString *)asJSONString
{
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:[self asJSONObject] options:0x0 error:&error];
    if (error)
    {
        return nil;
    }
    else
    {
        return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    }
}

- (id)asObjectFromJSONObject
{
    return self;
}

- (void)awakeFromJSONDict:(NSDictionary *)dict
{
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wundeclared-selector"
    for (NSString *propertyName in [[self class] performSelector:@selector(jsonProperties)])
#pragma clang diagnostic pop
    {
        NSMutableString *setterName = [NSMutableString stringWithString:@"set"];
        [setterName appendString:[[propertyName substringToIndex:1] capitalizedString]];
        if ([propertyName length] > 1)
        {
            [setterName appendString:[propertyName substringFromIndex:1]];
        }
        [setterName appendString:@":"];
        //NSLog(@"%@", propertyName);
        //NSLog(@"%@", setterName);
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        if ([self respondsToSelector:NSSelectorFromString(setterName)])
        {
            [self performSelector:NSSelectorFromString(setterName)
                       withObject:[[dict objectForKey:propertyName] asObjectFromJSONObject]];
        }
#pragma clang diagnostic pop
    }

}

@end
