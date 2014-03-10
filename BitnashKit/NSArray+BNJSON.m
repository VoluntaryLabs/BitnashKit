//
//  NSArray+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "NSArray+BNJSON.h"
#import "NSObject+BNJSON.h"

@implementation NSArray (BNJSON)

- asJSONObject
{
    NSMutableArray *array = [NSMutableArray array];
    for (id obj in self)
    {
        [array addObject:[obj asJSONObject]];
    }
    return array;
}

- (id)asObjectFromJSONObject
{
    NSMutableArray *array = [NSMutableArray array];
    for (id obj in self)
    {
        [array addObject:[obj asObjectFromJSONObject]];
    }
    return array;
}

@end
