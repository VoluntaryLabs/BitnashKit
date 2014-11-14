//
//  NSDictionary+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "NSDictionary+BN.h"
#import "BNObject.h"
#import "NSArray+BN.h"

@implementation NSDictionary (BN)

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

- (void)setBnParent:(BNObject *)bnParent
{
    [[self allValues] setBnParent:bnParent];
}

@end
