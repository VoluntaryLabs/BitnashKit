//
//  NSObject+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "NSObject+BN.h"

@implementation NSObject (BN)

- (id)asJSONObject
{
    return self;
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

@end
