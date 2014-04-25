//
//  NSDictionary+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "NSDictionary+BNJSON.h"
#import "BNObject.h"

@implementation NSDictionary (BNJSON)

- asJSONObject
{
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    for (NSString *key in self)
    {
        [dict setObject:[[self objectForKey:key] asJSONObject] forKey:key];
    }
    return dict;
}

- (id)asObjectFromJSONObject
{
    NSString *objType = [self objectForKey:@"type"];
    if (objType)
    {
        Class objClass = NSClassFromString(objType);
        BNObject *obj = [[objClass alloc] init];
        [obj awakeFromJSONDict:self];
        return obj;
    }
    else
    {
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];
        for (NSString *key in self)
        {
            [dict setObject:[[self objectForKey:key] asObjectFromJSONObject] forKey:key];
        }
        return dict;
    }
}

@end
